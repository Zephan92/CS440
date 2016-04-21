import Text.ParserCombinators.Parsec

data Exp = IntExp Integer
         | SymExp String
         | SExp [Exp]
         deriving (Show)

data Val = IntVal Integer
         | SymVal String
         | PrimVal ([Val] -> Val)
         | Closure [String] Exp Env
         | Macro [String] Exp Env
         | DefVal String Val
         | ConsVal Val Val

type Env = [(String, Val)]

run x = parseTest x

-- Lexicals

adigit = oneOf ['0'..'9']
digits = many1 adigit

nonNum = "-*+/:'?><=" ++ ['a'..'z'] ++ ['A'..'Z']
symChar = nonNum ++ ['0'..'9']

aSymbol = do f <- oneOf nonNum
             r <- many (oneOf symChar)
             ws
             return (f : r)
             

aSymbolExp = do s <- aSymbol
                return (SymExp s)

anUnquote = do oneOf ","
               e <- anExp
               return (SExp [SymExp "unquote", e])

aQQExp = do oneOf "`"
            e <- anExp
            return (SExp [SymExp "quasiquote", e])

aQExp = do oneOf "'"
           e <- anExp
           return (SExp [SymExp "quote", e])

anSExp = do oneOf "(" --defines a list
            ws
            arglist <- many anExp
            oneOf ")"
            ws
            return (SExp arglist)

ws = many (oneOf " \t\n") --white space consumer

-- Grammaticals

anInt = do d <- digits
           ws
           return $ IntExp (read d)

anAtom = anInt
         <|> aSymbolExp

anExp = aQExp
        <|> aQQExp
        <|> anUnquote
        <|> anAtom
        <|> anSExp

-- Utilities

liftIntOp op base args = IntVal $ foldr op base $ map (\ (IntVal y) -> y) args

runtime = [("+", PrimVal $ liftIntOp (+) 0),
           ("-", PrimVal $ liftIntOp (-) 0),
           ("+", PrimVal $ liftIntOp (*) 1)]

evalHelper (Closure cargs exp cenv) args env = eval exp (zip cargs (map (\a -> eval a env) args) ++ cenv)

-- Evaluator

eval :: Exp -> [(String,Val)] -> Val
eval (IntExp i) env = IntVal i

eval (SymExp s) env = case lookup s env of
                      Just result -> result
                      Nothing -> error "Error"
                      
eval (SExp []) env = SymVal "nil"                      
eval (SExp (x:xs)) env = evalHelper (eval x env) xs env

--eval (SExp "define") env = SymVal "define SExp"

-- Printer

instance Show Val where
  show (IntVal i) = show i
  show (SymVal s) = show s

repl defs =
  do putStr "> "
     l <- getLine
     case parse anExp "Expression" l of
       Right exp -> putStr (show (eval exp defs))
       Left pe   -> putStr (show pe)
     putStrLn ""
     repl defs
          

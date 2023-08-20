# Инструкция
Программа предназначается для слияния предварительно отсортированных по возрастанию целых чисел или строк из нескольких файлов в один файл. При слиянии данные будут отсортированы по возрастанию.

Данная программа была разработана с помощью jdk версии 20 и собрана с помощью системы сборки Maven версии 3.8.1.
Используется сторонняя библиотека Guava версии 32.1.2-jre.

## Что нужно, чтобы запустить?
Перед запуском программы нужно будет установить  [нужную версию jdk для вашей операционной системы](https://www.oracle.com/java/technologies/downloads/).

### Чтобы запустить с помощью jar файла 
Чтобы запустить данную программу, сначала нужно зайти в командную строку. 
После этого либо перейдите в папку с приложением и напишите java -jar sort-it.jar, либо напишите java -jar <полный путь до sort-it.jar> и введите параметры через пробел: 
  1. <Тип даных>. Поддерживаются только целые числа (флаг -i) и строки (флаг -s).
  2. <Выходной файл>. Если хотите использовать файл в той же папке, что и приложение, то тогда просто напишите название файла. Иначе напишите полный путь до файла. Если файла не окажется в папке с приложением или в указанной вами папке, то приложение само создаст его там, если это возможно. Если создать или открыть файл не получится, то приложение выдаст об этом сообщение на экран.
  3. <Входные файлы...>. Все входные файлы также указываются через пробел. Если файл  находиться в той же папке, что и приложение, то тогда просто напишите название файла. Иначе напишите полный путь до файла. Если какого файла не будет существовать, то приложение скажет об этом и укажет название данного файла (файлов если их несколько).
После того, как вы ввели все входные параметры, нажмите клавишу Enter и дождитесь сообщения об успешном выполнении программы.
Если вы ввели не все параметры или указали какой-то с ошибкой, то приложение вам об этом сообщит и закончит свое выполнение. После устранения ошибок заново запустите приложение.

### Чтобы самому запустить проект



## Как она работает?



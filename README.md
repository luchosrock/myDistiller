# myDistiller
Self-Service Information Extraction.

**myDistiller** is a Java-based commandline tool to extract context aware information from natural language documents.
It creates xml documents from the files you drop in.
You can add your own semantics in order to retrieve the information useful to you.

# How to get started?

Copy myDistiller.jar to the location of your choice.
In the same directory you create a text file called Distiller4.config. Please make sure that the extension “config” is not altered by your system. Some editors, for instance, add “txt”, because they believe that’s the required extension.
We recommend that you copy Distiller4.config from the example and edit this copy.
You run myDistiller simply by the command "java -jar myDistiller.jar"
Alternatively you can implement your own distiller service using the source files available here.

# Configuration file

Distiller4.config has four entries:
The directory where myDistiller finds its input data, the original texts for instance.
It uses the output directory to store extracted data.
In the configuration directory it expects its auxiliary data and the patterns it shall operate on.
The language selection (the international language string). For the time being it handles en_US and es_ES

The structure of the entries:
CONFIG_DIR=your configuration directory
INPUT_DIR=your input directory
OUTPUT_DIR=your output directory
LOCALE=your locale

Example of the entries:
CONFIG_DIR=/Users/Literature/
INPUT_DIR=/Users/Literature/IN
OUTPUT_DIR=/Users/Literature/OUT
LOCALE=en_US

Please make sure that you have the correct language code in your configuration file! This is very important because myDistiller converts all numbers expressed in words into numbers. For instance, it turns "five" into "5". Of course, this conversion is language dependent. If you apply the Spanish version of this conversion (triggered by the code entry in your configuration file) to English texts, your results will be disappointing.
Supported input

myDistiller identifies the files it has to process by their file extensions. Valid extensions are listed in extensions.list.
For example:
txt
original

# Auxiliary files

myDistiller requires three auxiliary files. They serve to convert months, weekdays, and numbers, that are expressed by words, into numeric representations.

For English language support these files are
months.words
weekdays.words
number.words

For Spanish language support use:
meses.palabras
dias.palabras
numerales.palabras

These auxiliary data have the form number in words=number. myDistiller replaces number in words by number.
Example:
nineteen=19 in numbers.words
march=3 in months.words

# Document collections

Information extraction usually addresses different document collections having different and partly unique characteristics in terms of the information they cover. Invoices, for example, contain particular information (e.g. the addressee) that follow certain patterns. The same information (e.g. the same person) may be represented in contracts in a different way. There may be a generic pattern that covers both representation forms. However, you may also need different patterns, depending on your particular purpose. Very specific and complex patterns, for instance, tend to appear just in one data collection and nowhere else.
Each document class (invoices, emails, articles, etc.) that stands for each document collection has a unique identifier or title. myDistiller needs this title to separate incoming documents. It is therefore required to indicate the unique titles in the file identifiers.config. In addition, you choose a unique name for this collection that has this identifier.
In the absence of a declarative title the system automatically adds "ANY" as classification title and applies all generic patterns to this document.

# Example:
We have a number of articles of authors from Wikipedia. We call this collection “authors”. All its articles have as title “WIKIPEDIA” somewhere at the top of the article. We therefore add to the identifiers’ list:
authors=WIKIPEDIA

The label “authors” can also be used in the pattern files if we have specific patterns that should be applied only to documents from the collection containing Wikipedia articles on authors.

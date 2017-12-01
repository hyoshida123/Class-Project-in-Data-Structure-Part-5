package sbccunittest;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.junit.*;

import spellchecker.*;
import spellchecker.Dictionary;

public class SpellCheckerTester {

	public static String newline = System.getProperty("line.separator");

	public static int totalScore = 0;
	public static int extraCredit = 0;

	@BeforeClass
	public static void beforeTesting() {
		totalScore = 0;
		extraCredit = 0;
	}

	@AfterClass
	public static void afterTesting() {
		System.out.println("Estimated score (assuming no late penalties, etc.) = " + totalScore);
		System.out.println("Estimated extra credit (assuming on time submission) = " + extraCredit);
	}

	@Test(timeout = 10000)
	public void testImportFile() throws Exception {
		Dictionary dictionary = new BasicDictionary();

		dictionary.importFile("full_dictionary.txt");

		assertNotNull("Dictionary.getRoot() should not be null.", dictionary.getRoot());

		int depth = getTreeDepth(dictionary);
		int maxDepth = 100;
		if (depth > maxDepth)
			fail("The tree depth is " + depth + " is greater than the maximum allowed depth of " + maxDepth + ".");

		dictionary.save("full_dictionary.pre");
		String s = FileUtils.readFileToString(new File("full_dictionary.pre"));
		String[] parts = s.split("\n");
		assertEquals(175169, parts.length);

		totalScore += 12;
	}

	@Test(timeout = 10000)
	public void testImportFileCompleteTree() throws Exception {
		Dictionary dictionary = new BasicDictionary();
		dictionary.importFile("full_dictionary.txt");

		assertNotNull("Dictionary.getRoot() should not be null.", dictionary.getRoot());

		int depth = getTreeDepth(dictionary);
		int maxDepth = 18;
		if (depth > maxDepth)
			fail("The tree depth is " + depth + ", which is greater than the maximum allowed depth of " + maxDepth
					+ ".");

		dictionary.save("full_dictionary.pre");
		String s = FileUtils.readFileToString(new File("full_dictionary.pre"));
		String[] parts = s.split("\n");
		assertEquals(175169, parts.length);

		extraCredit += 5;
	}

	int treeDepth;

	public int getTreeDepth(Dictionary dictionary) {
		treeDepth = 0;
		goDeeper(dictionary.getRoot(), 0);
		return treeDepth;
	}

	private void goDeeper(BinaryTreeNode node, int depth) {
		if (node != null) {
			depth++;
			if (depth > treeDepth)
				treeDepth = depth;

			if (node.left != null)
				goDeeper(node.left, depth);
			if (node.right != null)
				goDeeper(node.right, depth);
		}
	}

	@Test(timeout = 10000)
	public void testLoad() throws Exception {
		Dictionary dictionary = new BasicDictionary();
		dictionary.load("dict_14.pre");

		assertNotNull("Dictionary.getRoot() should not be null.", dictionary.getRoot());

		int depth = getTreeDepth(dictionary);
		assertEquals(6, depth);

		totalScore += 8;
	}

	@Test(timeout = 10000)
	public void testSave() throws Exception {
		Dictionary dictionary = new BasicDictionary();
		String[] words = new String[] { "bull", "are", "genetic", "cotton", "dolly", "florida", "each", "bull" };
		for (String word : words)
			dictionary.add(word);

		dictionary.save("test_save.pre");
		String s = FileUtils.readFileToString(new File("test_save.pre"));
		String[] parts = s.split("\n");

		assertEquals(words.length - 1, parts.length);
		for (int ndx = 0; ndx < parts.length; ndx++)
			assertEquals(words[ndx], parts[ndx].trim().toLowerCase());

		totalScore += 8;
	}

	@Test(timeout = 10000)
	public void testFind() throws Exception {
		Dictionary dictionary = new BasicDictionary();
		String dictionaryPath = "dict_14.pre";
		dictionary.load(dictionaryPath);

		checkWord(dictionary, dictionaryPath, "cotton", null);
		checkWord(dictionary, dictionaryPath, "CottoN", null);
		checkWord(dictionary, dictionaryPath, "Cotto", new String[] { "bull", "cotton" });
		checkWord(dictionary, dictionaryPath, "mit", new String[] { "life", "mite" });
		checkWord(dictionary, dictionaryPath, "mite", null);
		checkWord(dictionary, dictionaryPath, "just", null);

		totalScore += 8;
	}

	private void checkWord(Dictionary dictionary, String dictionaryPath, String word, String[] expectedResult) {
		String[] result = dictionary.find(word);
		if (expectedResult != null) {
			if (result != null) {
				assertEquals(expectedResult[0], result[0]);
				assertEquals(expectedResult[1], result[1]);
			} else
				fail("Didn't find " + word + " in " + dictionaryPath);
		} else {
			if (result != null) {
				fail("The dictionary returned " + (result.length > 0 ? result[0] : "an empty array")
						+ " but should have returned null because " + word + " does exist in " + dictionaryPath);
			}
		}

	}

	@Test
	public void testLoadDocument() throws Exception {
		String dictionaryText = FileUtils.readFileToString(new File("small_dictionary.txt"));
		String[] words = dictionaryText.split(newline);
		Random rng = new Random();
		String doc = words[rng.nextInt(words.length)].trim() + " "
				+ words[rng.nextInt(words.length)].trim() + " "
				+ words[rng.nextInt(words.length)].trim() + " "
				+ words[rng.nextInt(words.length)].trim() + " "
				+ words[rng.nextInt(words.length)].trim();
		FileUtils.write(new File("random_doc.txt"), doc);
		SpellChecker basicSpellChecker = new BasicSpellChecker();
		basicSpellChecker.loadDocument("random_doc.txt");
		String text = basicSpellChecker.getText();
		assertEquals(doc, text);

		totalScore += 2;
	}

	@Test
	public void testSpellCheckWithOneUnknownWord() throws Exception {
		SpellChecker basicSpellChecker = new BasicSpellChecker();

		String dictionaryImportPath = "small_dictionary.txt";
		String dictionaryPath = "small_dictionary.pre";
		String documentPath = "small_document_one_unknown.txt";

		basicSpellChecker.importDictionary(dictionaryImportPath);
		basicSpellChecker.saveDictionary(dictionaryPath);

		basicSpellChecker.loadDocument(documentPath);

		String[] result;

		result = basicSpellChecker.spellCheck(false);
		if (result == null)
			fail("There should be one unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("explosins", result[0]);
			assertEquals("87", result[1]);
			assertEquals("ever", result[2]);
			assertEquals("explosions", result[3]);
		}

		totalScore += 6;
	}

	@Test
	public void testSpellCheckReplaceOneUnknownWord() throws Exception {
		SpellChecker basicSpellChecker = new BasicSpellChecker();

		String dictionaryImportPath = "small_dictionary.txt";
		String dictionaryPath = "small_dictionary.pre";
		String documentPath = "small_document_one_unknown.txt";

		basicSpellChecker.importDictionary(dictionaryImportPath);
		basicSpellChecker.saveDictionary(dictionaryPath);

		basicSpellChecker.loadDocument(documentPath);

		String[] result;

		// Spell-check and find one word misspelled.
		result = basicSpellChecker.spellCheck(false);
		if (result == null)
			fail("There should be one unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("explosins", result[0]);
			assertEquals("87", result[1]);
			assertEquals("ever", result[2]);
			assertEquals("explosions", result[3]);
		}

		// Replace it with the second suggestion.
		int startIndex = Integer.parseInt(result[1]);
		int endIndex = startIndex + result[0].length();
		basicSpellChecker.replaceText(startIndex, endIndex, result[3]);

		// Check against corrected.
		String text = basicSpellChecker.getText();
		String expected = FileUtils.readFileToString(new File("small_document_one_unknown_corrected.txt"));
		assertEquals(expected, text);

		totalScore += 6;
	}

	@Test
	public void testSpellCheckNoUnknownWords() throws Exception {
		SpellChecker basicSpellChecker = new BasicSpellChecker();

		String dictionaryImportPath = "small_dictionary.txt";
		String dictionaryPath = "small_dictionary.pre";
		String documentPath = "small_document.txt";

		basicSpellChecker.importDictionary(dictionaryImportPath);
		basicSpellChecker.saveDictionary(dictionaryPath);

		basicSpellChecker.loadDocument(documentPath);

		String[] result;

		result = basicSpellChecker.spellCheck(false);
		if (result != null)
			fail("There should be no unknown words in " + documentPath + " when the dictionary is " + dictionaryPath);

		totalScore += 4;
	}

	@Test
	public void testSpellCheckReplaceUnknowns() throws Exception {
		SpellChecker basicSpellChecker = new BasicSpellChecker();

		String dictionaryImportPath = "small_dictionary.txt";
		String dictionaryPath = "small_dictionary.pre";
		String documentPath = "small_document_four_unknown.txt";

		basicSpellChecker.importDictionary(dictionaryImportPath);
		basicSpellChecker.saveDictionary(dictionaryPath);

		basicSpellChecker.loadDocument(documentPath);

		String[] result;

		// Find first unknown
		result = basicSpellChecker.spellCheck(false);
		if (result == null)
			fail("Failed to find the first unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("explosins", result[0]);
			assertEquals("87", result[1]);
			assertEquals("ever", result[2]);
			assertEquals("explosions", result[3]);
		}

		// Replace it with the successor word
		int startIndex = Integer.parseInt(result[1]);
		int endIndex = startIndex + result[0].length();
		basicSpellChecker.replaceText(startIndex, endIndex, result[3]);

		// find the 2nd unknown (the word "which")
		result = basicSpellChecker.spellCheck(true);
		if (result == null)
			fail("Failed to find the second unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("which", result[0]);
			assertEquals("130", result[1]);
			assertEquals("use", result[2]);
			assertEquals("with", result[3]);
		}

		// Add this word to the dictionary
		String wordToAdd = result[0];
		basicSpellChecker.addWordToDictionary(result[0]);

		// find the 3rd unknown (the word "vast")
		result = basicSpellChecker.spellCheck(true);
		if (result == null)
			fail("Failed to find the third unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("vast", result[0]);
			assertEquals("275", result[1]);
			assertEquals("use", result[2]);
			assertEquals("which", result[3]);
		}

		// Find third unknown
		result = basicSpellChecker.spellCheck(true);
		if (result == null)
			fail("Failed to find the fourth unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("cuosmos", result[0]);
			assertEquals("280", result[1]);
			assertEquals("cosmos", result[2]);
			assertEquals("dozen", result[3]);
		}

		// Replace it with the predecessor word
		startIndex = Integer.parseInt(result[1]);
		endIndex = startIndex + result[0].length();
		basicSpellChecker.replaceText(startIndex, endIndex, result[2]);

		// Verify document is correct
		String expectedText = FileUtils.readFileToString(new File("small_document_four_unknown_corrected.txt"));
		String actualText = basicSpellChecker.getText();
		assertEquals(expectedText, actualText);

		// Verify the saved document is correct
		basicSpellChecker.saveDocument("small_document_four_unknown_after_spellchecking.txt");
		actualText = FileUtils.readFileToString(new File("small_document_four_unknown_after_spellchecking.txt"));
		assertEquals(expectedText, actualText);

		// Verify the dictionary is correct
		basicSpellChecker.saveDictionary("small_dictionary_after_spellchecking.pre");
		String dictText = FileUtils.readFileToString(new File("small_dictionary_after_spellchecking.pre"));

		if (!dictText.contains(wordToAdd))
			fail("Dictionary file didn't contain " + wordToAdd + ".");

		totalScore += 4;
	}

	@Test
	public void testSpellCheckNoSuccessor() throws Exception {

		SpellChecker basicSpellChecker = new BasicSpellChecker();
		String dictionaryImportPath = "small_dictionary.txt";
		String dictionaryPath = "small_dictionary.pre";
		String documentPath = "small_document_test_no_successor.txt";

		basicSpellChecker.importDictionary(dictionaryImportPath);
		basicSpellChecker.saveDictionary(dictionaryPath);

		basicSpellChecker.loadDocument(documentPath);

		String[] result;

		// Find first unknown
		result = basicSpellChecker.spellCheck(false);
		if (result == null)
			fail("Failed to find the first unknown word in " + documentPath + " when the dictionary is "
					+ dictionaryImportPath);
		else {
			assertEquals("zebras", result[0]);
			assertEquals("87", result[1]);
			assertEquals("with", result[2]);
			assertEquals("", result[3]);
		}

		totalScore += 2;
	}

}

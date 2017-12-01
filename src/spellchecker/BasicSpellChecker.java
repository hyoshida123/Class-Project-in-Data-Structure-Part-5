package spellchecker;

import java.io.*;
import java.util.regex.*;

import org.apache.commons.io.*;

public class BasicSpellChecker implements SpellChecker {

	private BasicDictionary bd = new BasicDictionary();
	private StringBuilder text;
	private Pattern p = Pattern.compile("\\b[\\w']+\\b");
	private Matcher m;

	@Override
	public void importDictionary(String filename) throws Exception {
		bd.importFile(filename);
	}

	@Override
	public void loadDictionary(String filename) throws Exception {
		bd.load(filename);
	}

	@Override
	public void saveDictionary(String filename) throws Exception {
		bd.save(filename);
	}

	@Override
	public void loadDocument(String filename) throws Exception {
		text = new StringBuilder(FileUtils.readFileToString(new File(filename)));
	}

	@Override
	public void saveDocument(String filename) throws Exception {
		FileUtils.writeStringToFile(new File(filename), text.toString());
	}

	@Override
	public String getText() {
		return text.toString();
	}

	@Override
	public String[] spellCheck(boolean continueFromPrevious) {
		int ndx = 0;
		String[] ret = null;
		if (m == null || continueFromPrevious != true)
			m = p.matcher(text);
		while (m.find()) {
			String word = m.group();
			String[] result = bd.find(word);
			if (result != null) {
				ret = new String[4];
				ret[0] = word;
				ret[1] = Integer.toString(m.start());
				ret[2] = result[0];
				ret[3] = result[1];
				ndx = m.start();
				return ret;
			}
		}

		return null;
	}

	@Override
	public void addWordToDictionary(String word) {
		bd.add(word);
	}

	@Override
	public void replaceText(int startIndex, int endIndex, String replacementText) {
		// text = text.substring(0, startIndex) + replacementText +
		// text.substring(endIndex);
		text.replace(startIndex, endIndex, replacementText);
	}

}

package uk.co.flax.luwak.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.flax.luwak.*;
import uk.co.flax.luwak.matchers.HighlightingMatcher;
import uk.co.flax.luwak.matchers.HighlightsMatch;
import uk.co.flax.luwak.presearcher.TermFilteredPresearcher;
import uk.co.flax.luwak.queryparsers.LuceneQueryParser;
import uk.co.flax.luwak.termextractor.weights.TermWeightor;

/*
 * Copyright (c) 2013 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LuwakDemo {

    public static final Analyzer ANALYZER = new StandardAnalyzer();

    public static final String FIELD = "text";

    public static final Logger logger = LoggerFactory.getLogger(LuwakDemo.class);

    public static void main(String... args) throws Exception {
        new LuwakDemo("demoqueries", "gutenberg");
    }

    public LuwakDemo(String queriesFile, String inputDirectory) throws Exception {

        try (Monitor monitor = new Monitor(new LuceneQueryParser(FIELD, ANALYZER), new TermFilteredPresearcher())) {
            addQueries(monitor, queriesFile);
            DocumentBatch batch = DocumentBatch.of(buildDocs(inputDirectory));
            Matches<HighlightsMatch> matches = monitor.match(batch, HighlightingMatcher.FACTORY);
            outputMatches(matches);
        }
    }

    static void addQueries(Monitor monitor, String queriesFile) throws Exception {
        List<MonitorQuery> queries = new ArrayList<>();
        int count = 0;
        logger.info("Loading queries from {}", queriesFile);
        File queriesFiles = getFileFromResource(queriesFile);
        try (BufferedReader br = new BufferedReader(new FileReader(queriesFiles))) {
            String queryString;
            while ((queryString = br.readLine()) != null) {
                if (Strings.isNullOrEmpty(queryString))
                    continue;
                logger.info("Parsing [{}]", queryString);
                queries.add(new MonitorQuery(String.format(Locale.ROOT, "%d-%s", count++, queryString), queryString));
            }
        }
        monitor.update(queries);
        logger.info("Added {} queries to monitor", count);
    }

    static List<InputDocument> buildDocs(String inputDirectory) throws Exception {
        List<InputDocument> docs = new ArrayList<>();
        logger.info("Reading documents from {}", inputDirectory);

        for (File file : getFilesFromResource(inputDirectory)) {
            String content;
            try (FileInputStream fis = new FileInputStream(file);
                 InputStreamReader reader = new InputStreamReader(fis, Charsets.UTF_8)) {
                content = CharStreams.toString(reader);
                InputDocument doc = InputDocument.builder(file.getPath())
                        .addField(FIELD, content, new StandardAnalyzer())
                        .build();
                docs.add(doc);
            }
        }
        return docs;
    }

    static void outputMatches(Matches<HighlightsMatch> matches) {

        logger.info("Matched batch of {} documents in {} milliseconds with {} queries run",
                matches.getBatchSize(), matches.getSearchTime(), matches.getQueriesRun());
        for (DocumentMatches<HighlightsMatch> docMatches : matches) {
            logger.info("Matches from {}", docMatches.getDocId());
            for (HighlightsMatch match : docMatches) {
                logger.info("\tQuery: {} ({} hits)", match.getQueryId(), match.getHitCount());
            }
        }

    }

    private static File getFileFromResource(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    private static File[] getFilesFromResource(String folder) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }

}

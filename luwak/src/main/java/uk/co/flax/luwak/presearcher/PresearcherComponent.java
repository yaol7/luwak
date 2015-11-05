package uk.co.flax.luwak.presearcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import uk.co.flax.luwak.termextractor.QueryTerm;
import uk.co.flax.luwak.termextractor.QueryTreeBuilder;

/*
 * Copyright (c) 2014 Lemur Consulting Ltd.
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

/**
 * Class that wraps a set of Presearcher behaviours
 */
public class PresearcherComponent {

    private final List<? extends QueryTreeBuilder<?>> builders;

    /**
     * Create a new PresearcherComponent from a list of QueryTreeBuilders
     * @param builders the builders
     */
    public PresearcherComponent(List<? extends QueryTreeBuilder<?>> builders) {
        this.builders = builders;
    }

    /**
     * Create a new PresearcherComponent from a list of QueryTreeBuilders
     * @param builders the builders
     */
    public PresearcherComponent(QueryTreeBuilder<?>... builders) {
        this(Arrays.asList(builders));
    }

    /**
     * @return the QueryTreeBuilders for this component
     */
    public List<? extends QueryTreeBuilder<?>> getQueryTreeBuilders() {
        return builders;
    }

    /**
     * Filter the TokenStream used by the Presearcher to create it's document query
     * @param field the field for this TokenStream
     * @param ts a TokenStream generated by examining the presearcher's InputDocument
     * @return a filtered TokenStream
     */
    public TokenStream filterDocumentTokens(String field, TokenStream ts) {
        return ts;
    }

    /**
     * Add an extra token to the Document used to index a Query
     *
     * For example, if one of the QueryTreeBuilders injects a CUSTOM QueryTerm.Type
     * into the query tree, and the relevant term is collected, you may want to add
     * a specific token here
     *
     * @param term the collected QueryTerm
     *
     * @return a token to index, or null if no extra token is required
     */
    public BytesRef extraToken(QueryTerm term) {
        return null;
    }

    /**
     * Make changes to the presearcher query built from an InputDocument.  By default,
     * does nothing.
     *
     * @param reader a LeafReader over the input documents index
     * @param presearcherQuery the input query built from the InputDocument
     *
     * @throws java.io.IOException on I/O errors
     *
     * @return an adjusted query
     */
    public Query adjustPresearcherQuery(LeafReader reader, Query presearcherQuery) throws IOException {
        return presearcherQuery;
    }

    /**
     * Make changes to the lucene Document that describes how a MonitorQuery will
     * be indexed by the presearcher.
     * @param doc the lucene Document
     * @param metadata MonitorQuery metadata
     */
    public void adjustQueryDocument(Document doc, Map<String, String> metadata) {

    }
}

/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.expression.functions;

import com.google.common.base.Joiner;
import org.auraframework.Aura;
import org.auraframework.def.ApplicationDef;
import org.auraframework.throwable.quickfix.QuickFixException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * An implementation of utility functions to mimic Util.js.
 */
public class UtilFunctions {

    public static final Function EMPTY = new Empty();
    public static final Function FORMAT = new Format();
    public static final Function TOKEN = new Token();
    public static final Function JOIN = new Join();

    /**
     * Checks if the object is empty. An empty object"s value is undefined (only
     * in JS), null, an empty array, or empty string. An object with no native
     * properties is not considered empty at this time.
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return "".equals(obj);
        }
        if (obj instanceof List) {
            return ((List<?>) obj).isEmpty();
        }
        return false;
    }

    /**
     * Empty is meant to match isEmpty() in Util.js
     */
    public static class Empty extends BaseUnaryFunction {
        private static final long serialVersionUID = -8834318118368934926L;

        @Override
        public Object evaluate(Object arg) {
            return Boolean.valueOf(isEmpty(arg));
        }

        @Override
        public String getJsFunction() {
            return "fn.empty";
        }

        @Override
        public String[] getKeys() {
            return new String[] { "empty" };
        }
    }

    /**
     * Format is meant to match format() in Util.js, except
     * that we safeguard against missing, undefined, or null
     * format string.
     *
     * Since expressions are exposed to the UI, we try to do
     * the most sensible thing, and prevent the display of nulls
     * and undefined like we do with the ADD function.
     */
    public static class Format extends BaseMultiFunction {

        private static final long serialVersionUID = -7261120970634674388L;

        @Override
        public Object evaluate(List<Object> args) {
            int size = args.size();
            if (size == 0) {
                return "";
            }

            Object a0 = args.get(0);
            if (a0 == null) {
                return "";
            }

            String formatString = JavascriptHelpers.stringify(a0);
            if (size == 1) {
                return formatString;
            }

            Object[] formatArguments = new Object[size - 1];
            for (int index = 1; index < size; index++) {
                Object ai = args.get(index);
                formatArguments[index - 1] = (ai == null) ? "" : JavascriptHelpers.stringify(ai);
            }

            return MessageFormat.format(formatString, formatArguments);
        }

        @Override
        public String getJsFunction() {
            return "fn.format";
        }

        @Override
        public String[] getKeys() {
            return new String[] { "format" };
        }
    }

    /**
     * Token is meant for application level configuration injection
     */
    public static class Token extends BaseUnaryFunction {
        private static final long serialVersionUID = -8834318318368934926L;

        @Override
        public Object evaluate(Object arg) {
            try {
                Map<String,String> tokens = ((ApplicationDef)Aura.getContextService().getCurrentContext().getApplicationDescriptor().getDef()).getTokens();
                if (tokens != null) {
                    if (arg != null && tokens.containsKey(arg)) {
                        return tokens.get(arg);
                    }
                }
            } catch (ClassCastException exception) {
                //??
                return "AURA: INVALID APPLICATION";
            } catch (QuickFixException exception) {
                //??
            }
            return "";
        }

        @Override
        public String getJsFunction() {
            return "fn.token";
        }

        @Override
        public String[] getKeys() {
            return new String[] {"token", "t"};
        }
    }

    /**
     * Join is meant to concatenate an iterable with a separator
     */
    public static class Join extends BaseMultiFunction {
        private static final long serialVersionUID = -8834318318368762326L;

        @Override
        public Object evaluate(List<Object> args) {
            int size = args.size();
            if (size < 2){
                return "";
            }
            if (size == 2){
                return JavascriptHelpers.stringify(args.get(1));
            }
            Object a0 = args.get(0);
            String separator=JavascriptHelpers.stringify(a0 != null ? a0 : "");
            Object[] formatArguments = new Object[size - 1];
            for (int index = 1; index < size; index++) {
                formatArguments[index - 1] = JavascriptHelpers.stringify(args.get(index));
            }

            return Joiner.on(separator).skipNulls().join(formatArguments);
        }

        @Override
        public String getJsFunction() {
            return "fn.join";
        }

        @Override
        public String[] getKeys() {
            return new String[] { "join" };
        }
    }
}

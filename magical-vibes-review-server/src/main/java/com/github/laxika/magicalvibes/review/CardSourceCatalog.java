package com.github.laxika.magicalvibes.review;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Discovers registrations from live source files without initializing game cards or fetching oracle data. */
@Component
public class CardSourceCatalog {
    private static final Pattern REGISTRATION = Pattern.compile(
            "@CardRegistration\\(\\s*set\\s*=\\s*\"([^\"]+)\"\\s*,\\s*collectorNumber\\s*=\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern PACKAGE = Pattern.compile("\\bpackage\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS = Pattern.compile("\\bclass\\s+(\\w+)\\s+extends\\s+Card\\b");
    private final Path root;
    private final ObjectMapper mapper;

    public CardSourceCatalog(Path reviewRepositoryRoot, ObjectMapper mapper) {
        this.root = reviewRepositoryRoot;
        this.mapper = mapper;
    }

    public List<SourceCard> scan() throws IOException {
        Path directory = root.resolve("magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards");
        Map<String, Map<String, String>> names = new HashMap<>();
        List<SourceCard> cards = new ArrayList<>();
        try (var files = Files.walk(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
                String source = withoutComments(Files.readString(file));
                var packageMatch = PACKAGE.matcher(source);
                var classMatch = CLASS.matcher(source);
                if (!packageMatch.find() || !classMatch.find()) {
                    continue;
                }
                var registrations = REGISTRATION.matcher(source);
                List<Printing> printings = new ArrayList<>();
                while (registrations.find()) {
                    Printing printing = new Printing(registrations.group(1).toUpperCase(Locale.ROOT), registrations.group(2));
                    if (!printings.contains(printing)) {
                        printings.add(printing);
                    }
                }
                if (printings.isEmpty()) {
                    continue;
                }
                String simpleName = classMatch.group(1);
                String displayName = simpleName.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z0-9])(?=[A-Z])", " ");
                for (Printing printing : printings) {
                    String cachedName = names.computeIfAbsent(printing.setCode(), this::cachedNames)
                            .get(printing.collectorNumber());
                    if (cachedName != null) {
                        displayName = cachedName;
                        break;
                    }
                }
                cards.add(new SourceCard(packageMatch.group(1) + "." + simpleName, displayName,
                        root.relativize(file).toString().replace('\\', '/'), List.copyOf(printings)));
            }
        }
        cards.sort(Comparator.comparing(SourceCard::className));
        return List.copyOf(cards);
    }

    private Map<String, String> cachedNames(String set) {
        Path path = root.resolve("mcp/card-info/cache/" + set.toLowerCase(Locale.ROOT) + ".json");
        Map<String, String> result = new HashMap<>();
        if (Files.exists(path)) {
            try {
                for (var card : mapper.readTree(Files.readString(path)).path("cards")) {
                    if (card.hasNonNull("collector_number") && card.hasNonNull("name")) {
                        result.put(card.path("collector_number").asText(), card.path("name").asText());
                    }
                }
            } catch (RuntimeException | IOException ignored) {
                // Names are optional; an unavailable cache must not hide implemented cards.
            }
        }
        return result;
    }

    /** Removes comments while preserving quoted annotation values, including URLs inside strings. */
    static String withoutComments(String source) {
        var tokens = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/")
                .matcher(source);
        StringBuilder result = new StringBuilder();
        while (tokens.find()) {
            tokens.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(
                    tokens.group().startsWith("/") ? " " : tokens.group()));
        }
        tokens.appendTail(result);
        return result.toString();
    }

    public record Printing(String setCode, String collectorNumber) {}
    public record SourceCard(String className, String displayName, String sourcePath, List<Printing> printings) {}
}

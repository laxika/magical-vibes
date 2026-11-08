package com.github.laxika.magicalvibes.architecture;

import com.github.laxika.magicalvibes.model.StackEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ratchet for {@link StackEntry#copyTriggerEventFrom}: the one place that copies what a trigger event was about, which
 * both the copy constructor and a copied ability ({@code CopySupport.createCopyStackEntry}) use. A trigger-event field
 * copied only by the copy constructor would again be lost by every copy of a triggered ability (Strionic Resonator,
 * Lithoform Engine, Tawnos), as the triggering permanent was after step 15 run 2 of the EffectSlot refactor.
 *
 * <p>A trigger-event field is one whose name starts with {@code triggering} or {@code event}, or ends with
 * {@code AtTrigger}.
 */
class TriggerEventCopyCompletenessTest {

    private static final String STACK_ENTRY_SRC =
            "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/StackEntry.java";
    private static final Pattern TRIGGER_EVENT_FIELD = Pattern.compile("^(triggering|event)[A-Z].*|.*AtTrigger$");

    @Test
    @DisplayName("copyTriggerEventFrom copies every trigger-event field of StackEntry")
    void copyTriggerEventFromCoversEveryTriggerEventField() throws IOException {
        String body = methodBody(readStackEntrySource(), "public void copyTriggerEventFrom(StackEntry source)");

        List<String> missing = new ArrayList<>();
        for (Field field : StackEntry.class.getDeclaredFields()) {
            String name = field.getName();
            if (Modifier.isStatic(field.getModifiers()) || !TRIGGER_EVENT_FIELD.matcher(name).matches()) {
                continue;
            }
            if (!Pattern.compile("\\bsource\\." + Pattern.quote(name) + "\\b").matcher(body).find()) {
                missing.add(name);
            }
        }

        assertThat(missing)
                .as("These trigger-event fields of StackEntry are not copied by copyTriggerEventFrom, so a copy of a "
                        + "triggered ability loses them. Copy each one there instead of in the copy constructor.")
                .isEmpty();
    }

    private static String readStackEntrySource() throws IOException {
        Path source = locateRepoRoot().resolve(STACK_ENTRY_SRC);
        assertThat(Files.exists(source)).as("StackEntry source not found at %s", source).isTrue();
        return Files.readString(source, StandardCharsets.UTF_8);
    }

    private static String methodBody(String source, String declaration) {
        int start = source.indexOf(declaration);
        assertThat(start).as("%s not found in StackEntry", declaration).isNotEqualTo(-1);
        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(open, i + 1);
                }
            }
        }
        throw new AssertionError("Unbalanced braces after " + declaration);
    }

    private static Path locateRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve(STACK_ENTRY_SRC))) {
            dir = dir.getParent();
        }
        assertThat(dir).as("repository root containing %s", STACK_ENTRY_SRC).isNotNull();
        return dir;
    }
}

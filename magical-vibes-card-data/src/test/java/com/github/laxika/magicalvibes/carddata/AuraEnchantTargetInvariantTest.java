package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the aura enchant-restriction invariant: the engine has NO generic "Enchant creature"
 * enforcement — an aura's enchant restriction exists only as the target filter its card class
 * declares via {@code target(...)}. A class that omits it lets every validation path (UI
 * enumeration, cast-time validation, and the AI's target picker) attach the aura to ANY
 * permanent, including lands (the Spirit-Link-on-a-Mountain bug, July 2026).
 *
 * <p>For every registered printing whose oracle type line makes it an Aura, this test reads the
 * "Enchant X" line of the oracle text and asserts the constructed card class declares the
 * matching restriction:
 * <ul>
 * <li>"Enchant permanent" — no filter required (any permanent is a legal target).</li>
 * <li>"Enchant player" / "Enchant opponent" — the card must report {@link Card#isEnchantPlayer()}
 * (automatic for Curses; others must call {@code setEnchantPlayer(true)}).</li>
 * <li>Anything else ("Enchant creature", "Enchant land", "Enchant creature you control", …) —
 * the card must declare a non-null target filter via {@code target(...)}.</li>
 * </ul>
 *
 * <p>Note the test only asserts a filter EXISTS, not that it matches the enchant type exactly —
 * per-card tests own that. Back-face aura classes (e.g. Ghastly Haunting) have no printing of
 * their own and attach via effects rather than casting, so they are naturally out of scope.
 */
@Tag("scryfall")
class AuraEnchantTargetInvariantTest {

    private static final String CACHE_DIR = "./card-data-cache";
    private static final Pattern ENCHANT_LINE = Pattern.compile("^Enchant (.+)$", Pattern.MULTILINE);

    @Test
    void everyEnchantRestrictedAuraDeclaresItsTargetRestriction() {
        Card.clearOracleRegistry();
        MtgjsonOracleLoader.loadAll(CACHE_DIR);

        List<String> violations = new ArrayList<>();
        Set<String> checkedClasses = new HashSet<>();
        int aurasChecked = 0;

        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : set.getPrintings()) {
                Card card = printing.factory().get();
                String className = card.getClass().getSimpleName();
                if (!checkedClasses.add(className)) {
                    continue;
                }
                if (!card.isAura()) {
                    continue;
                }
                aurasChecked++;

                String label = card.getName() + " [" + className + ", " + set.getCode()
                        + " #" + printing.collectorNumber() + "]";
                String text = card.getCardText();
                Matcher enchant = text != null ? ENCHANT_LINE.matcher(text) : null;
                if (enchant == null || !enchant.find()) {
                    violations.add(label + ": Aura without an 'Enchant ...' oracle line — check the card data");
                    continue;
                }

                String enchantType = enchant.group(1).strip();
                if (enchantType.equalsIgnoreCase("permanent")) {
                    continue;
                }
                if (enchantType.equalsIgnoreCase("player") || enchantType.equalsIgnoreCase("opponent")) {
                    if (!card.isEnchantPlayer()) {
                        violations.add(label + ": 'Enchant " + enchantType + "' but isEnchantPlayer() is false"
                                + " — non-Curse enchant-player auras must call setEnchantPlayer(true)");
                    }
                    continue;
                }
                if (card.getTargetFilter() == null) {
                    violations.add(label + ": 'Enchant " + enchantType + "' but the class declares no"
                            + " target(...) filter — every validation path would let it attach to any"
                            + " permanent, including lands");
                }
            }
        }

        assertThat(aurasChecked).as("sanity: registered aura classes found").isGreaterThan(100);
        assertThat(violations)
                .as("auras whose class does not declare the oracle 'Enchant ...' restriction")
                .isEmpty();
    }
}

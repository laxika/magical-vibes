package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A {@code @CardRegistration} collector number is typed by hand, and pointing one at the wrong card
 * used to be undetectable: the class kept its own engine logic and silently adopted a different
 * card's printed characteristics. These cases pin the load failure that replaced that, and the
 * spellings that must keep loading — every one of them taken from a class that exists today.
 */
class MisfiledPrintingRejectionTest {

    private static final CardSet SET = CardSet.SET_M15;

    private CardRegistry registry;

    @BeforeEach
    void clearOracleData() {
        Card.clearOracleRegistry();
    }

    @AfterEach
    void closeRegistry() {
        if (registry != null) {
            registry.close();
        }
        Card.clearOracleRegistry();
    }

    @Test
    void loadingASetFailsWhenAPrintingNamesADifferentCardThanItsClass() {
        CardPrinting misfiled = CardScanner.scan().get(SET).getFirst();

        assertThatThrownBy(() -> loadSetNaming(printing -> "Some Other Card"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SET.getCode() + " #" + misfiled.collectorNumber())
                .hasMessageContaining("Some Other Card")
                .hasMessageContaining(misfiled.cardClassName());
    }

    @Test
    void loadingASetSucceedsWhenEveryPrintingNamesItsOwnClass() {
        assertThatCode(() -> loadSetNaming(CardPrinting::simpleCardClassName))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsPunctuationAndSpacingAClassNameCannotHold() {
        assertThat(matches("WillOTheWisp", "Will-o'-the-Wisp")).isTrue();
        assertThat(matches("EightAndAHalfTails", "Eight-and-a-Half-Tails")).isTrue();
        assertThat(matches("MishrasSelfReplicator", "Mishra's Self-Replicator")).isTrue();
    }

    /** Both spellings occur in the card classes, so both have to be accepted. */
    @Test
    void acceptsAnAccentedLetterFoldedOntoItsBaseLetterOrDroppedOutright() {
        assertThat(matches("Seance", "Séance")).isTrue();
        assertThat(matches("DandN", "Dandân")).isTrue();
        assertThat(matches("LimDLsVault", "Lim-Dûl's Vault")).isTrue();
    }

    @Test
    void acceptsALegendaryNamedByThePartBeforeTheComma() {
        assertThat(matches("Slimefoot", "Slimefoot, the Stowaway")).isTrue();
        assertThat(matches("NissaVastwoodSeer", "Nissa, Vastwood Seer")).isTrue();
    }

    /** A transform card's class may be named after its front face alone, or after both faces. */
    @Test
    void acceptsEitherDoubleFacedNamingConvention() {
        assertThat(CardRegistry.matchesClassName(
                "JaceVrynsProdigy", "Jace, Vryn's Prodigy", "Jace, Telepath Unbound")).isTrue();
        assertThat(CardRegistry.matchesClassName(
                "LoyalCatharUnhallowedCathar", "Loyal Cathar", "Unhallowed Cathar")).isTrue();
        assertThat(CardRegistry.matchesClassName(
                "Elbrus", "Elbrus, the Binding Blade", "Withengar Unbound")).isTrue();
    }

    @Test
    void rejectsADifferentCardWhoseNameMerelyStartsOrEndsTheSameWay() {
        assertThat(matches("Disperse", "Void Snare")).isFalse();
        assertThat(matches("SulfurousSpring", "Sulfurous Springs")).isFalse();
        assertThat(matches("Shock", "Shock Troops")).isFalse();
        assertThat(CardRegistry.matchesClassName("UnhallowedCathar", "Loyal Cathar", "Unhallowed Cathar"))
                .isFalse();
    }

    private static boolean matches(String simpleClassName, String cardName) {
        return CardRegistry.matchesClassName(simpleClassName, cardName, null);
    }

    /** Loads {@link #SET} on demand from a loader that names every printing as {@code naming} says. */
    private void loadSetNaming(Function<CardPrinting, String> naming) {
        OracleLoader loader = (setCode, implemented) -> {
            Map<String, OracleData> fronts = new HashMap<>();
            for (CardPrinting printing : CardScanner.scan().get(CardSet.findByCode(setCode))) {
                if (implemented.contains(printing.collectorNumber())) {
                    fronts.put(printing.collectorNumber(), oracle(naming.apply(printing)));
                }
            }
            return new SetOracleData(null, fronts.size(), Map.of(), fronts, Map.of(), Map.of());
        };

        registry = new CardRegistry(loader, OracleLoadMode.ON_DEMAND);
        registry.load();
        registry.ensureSetLoaded(SET);
    }

    private static OracleData oracle(String name) {
        return new OracleData(name, CardType.ENCHANTMENT, Set.of(), "{1}{W}", null, List.of(),
                List.of(), Set.of(), List.of(), null, null, null, Set.of(), null, null, null);
    }
}

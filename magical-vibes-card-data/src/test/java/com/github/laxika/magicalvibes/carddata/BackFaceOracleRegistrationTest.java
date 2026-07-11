package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Back faces may name a standalone card class (prepare-spell cards reuse the real spell's class,
 * e.g. Blazing Firesinger // Seething Song reuses SeethingSong). Face-level oracle data can be
 * poorer than the standalone printing's (Scryfall face nodes may lack colors), so the standalone
 * registration must win no matter which set is loaded first.
 */
class BackFaceOracleRegistrationTest {

    static class StandaloneFirstProbeSpell extends Card {}

    static class BackFaceFirstProbeSpell extends Card {}

    static class BackFaceOnlyProbeSpell extends Card {}

    @Test
    void backFaceRegistrationDoesNotClobberStandalonePrintingData() {
        Card.registerOracle("StandaloneFirstProbeSpell", oracle(CardColor.RED, List.of(CardColor.RED)));
        Card.registerOracleIfAbsent("StandaloneFirstProbeSpell", oracle(null, List.of()));

        assertThat(new StandaloneFirstProbeSpell().getColor()).isEqualTo(CardColor.RED);
    }

    @Test
    void standalonePrintingRegistrationWinsWhenBackFaceLoadedFirst() {
        Card.registerOracleIfAbsent("BackFaceFirstProbeSpell", oracle(null, List.of()));
        Card.registerOracle("BackFaceFirstProbeSpell", oracle(CardColor.RED, List.of(CardColor.RED)));

        assertThat(new BackFaceFirstProbeSpell().getColor()).isEqualTo(CardColor.RED);
    }

    @Test
    void backFaceRegistrationFillsTheRegistryWhenClassHasNoOwnPrinting() {
        Card.registerOracleIfAbsent("BackFaceOnlyProbeSpell", oracle(null, List.of()));

        assertThat(new BackFaceOnlyProbeSpell().getName()).isEqualTo("Probe Spell");
    }

    private static OracleData oracle(CardColor color, List<CardColor> colors) {
        return new OracleData("Probe Spell", CardType.INSTANT, Set.of(), "{2}{R}", color, colors,
                Set.of(), List.of(), "Add {R}{R}{R}.", null, null, Set.of(), null, null);
    }
}

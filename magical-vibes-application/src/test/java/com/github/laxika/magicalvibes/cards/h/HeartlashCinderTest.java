package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.i.ImpelledGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartlashCinder.class, ImpelledGiant.class, HoofSkulkin.class})
class HeartlashCinderTest extends BaseCardTest {

    private void castCinderSpell(Player player) {
        harness.castFromHand(player, new HeartlashCinder(), "{1}{R}");
        harness.passBothPriorities(); // resolve the Cinder, queue ETB trigger
    }

    private Permanent castCinder(Player player) {
        castCinderSpell(player);
        harness.passBothPriorities(); // resolve ETB trigger
        return findPermanent(player, "Heartlash Cinder");
    }

    @Test
    @DisplayName("ETB boost equals the red mana symbols among your permanents (self included)")
    void etbBoostsByRedSymbols() {
        // Impelled Giant {4}{R}{R} = 2 red symbols; Cinder itself {1}{R} = 1. Total X = 3.
        addCreatureReady(player1, new ImpelledGiant());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(4); // 1 base + 3
        assertThat(gqs.getEffectiveToughness(gd, cinder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only red mana symbols count; non-red permanents contribute nothing")
    void etbCountsOnlyRedSymbols() {
        // Hoof Skulkin {3} = 0 red symbols; only the Cinder's own {1}{R} counts. X = 1.
        addCreatureReady(player1, new HoofSkulkin());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2); // 1 base + 1
    }

    @Test
    @DisplayName("Counts only red symbols among permanents controlled by the Cinder's controller")
    void etbIgnoresOpponentsPermanents() {
        addCreatureReady(player2, new ImpelledGiant());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2); // 1 base + Cinder's own symbol
    }

    @Test
    @DisplayName("Counts permanents added before the ETB trigger resolves")
    void etbCountsAtTriggerResolution() {
        castCinderSpell(player1);
        addCreatureReady(player1, new ImpelledGiant());

        harness.passBothPriorities();

        Permanent cinder = findPermanent(player1, "Heartlash Cinder");
        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(4); // 1 base + 1 + 2 red symbols
    }

    @Test
    @CardUsed(AltarOfThePantheon.class)
    @DisplayName("Devotion modifiers do not add extra Chroma symbols")
    void etbDoesNotCountDevotionModifiers() {
        addCreatureReady(player1, new AltarOfThePantheon());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2); // only Cinder has a red symbol
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostWearsOff() {
        Permanent cinder = castCinder(player1); // X = 1 (self only)
        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(1);
    }
}

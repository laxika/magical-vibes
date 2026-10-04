package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.f.FigureOfDestiny;
import com.github.laxika.magicalvibes.cards.i.ImpelledGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartlashCinder.class, ImpelledGiant.class, HoofSkulkin.class, AltarOfThePantheon.class, FigureOfDestiny.class})
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
    @DisplayName("Devotion modifiers do not add extra Chroma symbols")
    void etbDoesNotCountDevotionModifiers() {
        addCreatureReady(player1, new AltarOfThePantheon());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2); // only Cinder has a red symbol
    }

    @Test
    @DisplayName("Haste allows Heartlash Cinder to attack the turn it enters")
    void canAttackImmediately() {
        Permanent cinder = castCinder(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(cinder.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Hybrid red symbols count once and activated ability costs do not count")
    void etbCountsHybridManaCostOnly() {
        addCreatureReady(player1, new FigureOfDestiny());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cinder)).isEqualTo(1);
    }

    @Test
    @DisplayName("The resolved boost stays fixed when another red permanent enters")
    void resolvedBoostDoesNotRecalculate() {
        Permanent cinder = castCinder(player1);
        addCreatureReady(player1, new ImpelledGiant());

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Red cards in hand and graveyard do not contribute to Chroma")
    void etbIgnoresCardsOutsideBattlefield() {
        harness.setGraveyard(player1, List.of(new ImpelledGiant()));
        castCinderSpell(player1);
        harness.setHand(player1, List.of(new ImpelledGiant()));

        harness.passBothPriorities();

        Permanent cinder = findPermanent(player1, "Heartlash Cinder");
        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);
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

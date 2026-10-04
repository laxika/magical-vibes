package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrinningIgnus;
import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamescrollCelebrant.class, ChandraNalaar.class, GrizzlyBears.class,
        ProdigalPyromancer.class, DrannithHealer.class, GrinningIgnus.class})
class FlamescrollCelebrantTest extends BaseCardTest {

    @Test
    void dealsDamageWhenOpponentActivatesNonManaAbility() {
        addCreatureReady(player1, new FlamescrollCelebrant());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player2, 20);
        forceMainPhase(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent celebrant = addCreatureReady(player1, new FlamescrollCelebrant());
        int powerBefore = gqs.getEffectivePower(gd, celebrant);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, celebrant)).isEqualTo(powerBefore + 2);
    }

    @Test
    void revelInSilenceStopsOpponentsCastingAndActivatingLoyaltyAbilities() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        chandra.setSummoningSick(false);
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new FlamescrollCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playersSilencedThisTurn).contains(player2.getId());
        assertThat(gd.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn)
                .contains(player2.getId())
                .doesNotContain(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Flamescroll Celebrant");

        forceMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player2, 1, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentCyclingTriggersDamage() {
        addCreatureReady(player1, new FlamescrollCelebrant());
        harness.setHand(player2, List.of(new DrannithHealer()));
        harness.setLibrary(player2, List.of(new FlamescrollCelebrant()));
        harness.setLife(player2, 20);
        forceMainPhase(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Flamescroll Celebrant");
    }

    @Test
    void ownActivationDoesNotTriggerDamage() {
        addCreatureReady(player1, new FlamescrollCelebrant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void repeatedPumpsExpireAtEndOfTurnWithoutChangingToughness() {
        Permanent celebrant = addCreatureReady(player1, new FlamescrollCelebrant());
        int powerBefore = gqs.getEffectivePower(gd, celebrant);
        int toughnessBefore = gqs.getEffectiveToughness(gd, celebrant);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, celebrant)).isEqualTo(powerBefore + 4);
        assertThat(gqs.getEffectiveToughness(gd, celebrant)).isEqualTo(toughnessBefore);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, celebrant)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, celebrant)).isEqualTo(toughnessBefore);
    }

    @Test
    void frontFaceCanBeCastAsCreature() {
        forceMainPhase(player1);
        harness.setHand(player1, List.of(new FlamescrollCelebrant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flamescroll Celebrant");
        assertThat(gd.playersSilencedThisTurn).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void revelDoesNotCounterExistingAbilitiesAndExpiresNextTurn() {
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        forceMainPhase(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.setHand(player1, List.of(new FlamescrollCelebrant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, 1, List.of());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        forceMainPhase(player1);
        harness.setHand(player2, List.of(new FlamescrollCelebrant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castModalInstant(player2, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).contains(player1.getId()).doesNotContain(player2.getId());
    }

    @Test
    void opponentManaAbilityDoesNotTriggerDamage() {
        addCreatureReady(player1, new FlamescrollCelebrant());
        addCreatureReady(player2, new GrinningIgnus());
        harness.setLife(player2, 20);
        forceMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Grinning Ignus");
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

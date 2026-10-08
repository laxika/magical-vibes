package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveOfRats.class})
class WaveOfRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting does not grant blitz haste or delayed sacrifice")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rats = findPermanent(player1, "Wave of Rats");
        assertThat(gqs.hasKeyword(gd, rats, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rats);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.setLibrary(player1, List.of(new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent rats = findPermanent(player1, "Wave of Rats");
        assertThat(gqs.hasKeyword(gd, rats, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wave of Rats");
        harness.assertInHand(player1, "Wave of Rats");
    }

    @Test
    @DisplayName("Returns to the battlefield when it dies after dealing combat damage to a player")
    void returnsAfterDealingCombatDamageToPlayerThisTurn() {
        Permanent rats = addCreatureReady(player1, new WaveOfRats());
        rats.setAttacking(true);

        resolveCombat();
        rats.setMarkedDamage(rats.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wave of Rats");
        harness.assertNotInGraveyard(player1, "Wave of Rats");
    }

    @Test
    @DisplayName("Does not return when it dies without dealing combat damage to a player")
    void doesNotReturnWithoutCombatDamageToPlayer() {
        Permanent rats = addCreatureReady(player1, new WaveOfRats());
        rats.setMarkedDamage(rats.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wave of Rats");
        harness.assertInGraveyard(player1, "Wave of Rats");
    }

    @Test
    @DisplayName("Blitz haste applies immediately without an enter-the-battlefield trigger")
    void blitzHasteAppliesImmediately() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreatureWithAlternateCost(player1, 0, List.of());
            harness.passBothPriorities();

            Permanent rats = findPermanent(player1, "Wave of Rats");
            assertThat(gqs.hasKeyword(gd, rats, Keyword.HASTE)).isTrue();
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Blitz creates exactly one sacrifice trigger at the next end step")
    void blitzCreatesOneEndStepSacrificeTrigger() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.setLibrary(player1, List.of(new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            harness.assertOnBattlefield(player1, "Wave of Rats");
            assertThat(gd.stack).hasSize(1);
        });
    }

    @Test
    @DisplayName("Blitz sacrifice after combat damage draws and returns a fresh creature")
    void blitzSacrificeReturnsWithoutBlitzBenefits() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.setLibrary(player1, List.of(new WaveOfRats(), new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent original = findPermanent(player1, "Wave of Rats");
        original.setAttacking(true);
        resolveCombat();
        harness.assertLife(player2, 16);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Wave of Rats");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        returned.setMarkedDamage(returned.getEffectiveToughness());
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Wave of Rats");
        harness.assertInGraveyard(player1, "Wave of Rats");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A pending return trigger cannot return a card that left and re-entered the graveyard")
    void returnDoesNotFollowCardThroughAnotherZoneChange() {
        Permanent original = addCreatureReady(player1, new WaveOfRats());
        original.setAttacking(true);
        resolveCombat();
        harness.assertLife(player2, 16);

        harness.withAutoStop(gd.currentStep, () -> {
            original.setMarkedDamage(original.getEffectiveToughness());
            harness.runStateBasedActions();
            assertThat(gd.stack).hasSize(1);

            harness.getPermanentRemovalService().removeCardFromGraveyardById(
                    gd, original.getCard().getId());
            Permanent reentered = new Permanent(original.getCard());
            harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                    gd, player1.getId(), reentered);
            reentered.setMarkedDamage(reentered.getEffectiveToughness());
            harness.runStateBasedActions();
            resolveAllTriggers();

            harness.assertNotOnBattlefield(player1, "Wave of Rats");
            harness.assertInGraveyard(player1, "Wave of Rats");
        });
    }
}

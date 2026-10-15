package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEleventhDoctor.class, Forest.class, GrizzlyBears.class})
class TheEleventhDoctorTest extends BaseCardTest {

    @Test
    void combatDamageMayExileAnyCardFromHandWithManaValueCounters() {
        addCreatureReady(player1, new TheEleventhDoctor());
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(land, creature));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.exiledCardTimeCounters).containsEntry(creature.getId(), 2);
    }

    @Test
    void activatedAbilityOnlyTargetsCreatureWithPowerThreeOrLess() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        largeCreature.setPowerModifier(2);
        largeCreature.setToughnessModifier(2);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityMakesAValidTargetUnblockableThisTurn() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void decliningCombatDamageAbilityLeavesCardInHand() {
        addCreatureReady(player1, new TheEleventhDoctor());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureGainsSuspendAndLosesACounterAtItsOwnersUpkeep() {
        addCreatureReady(player1, new TheEleventhDoctor());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
    }

    @Test
    void exilingAnotherCardDoesNotRemoveCountersFromPreviouslyExiledCards() {
        addCreatureReady(player1, new TheEleventhDoctor());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        findPermanent(player1, "The Eleventh Doctor").untap();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.exiledCardTimeCounters).containsEntry(first.getId(), 2)
                .containsEntry(second.getId(), 2);
    }

    @Test
    void landCanBeExiledButDoesNotImmediatelyReturnWithoutTimeCounters() {
        addCreatureReady(player1, new TheEleventhDoctor());
        Forest card = new Forest();
        harness.setHand(player1, List.of(card));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters.getOrDefault(card.getId(), 0)).isZero();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void targetBecomingTooPowerfulBeforeResolutionIsNotMadeUnblockable() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void opposingCreatureAtPowerThreeRemainsUnblockableIfPowerIncreasesAfterResolution() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(2);

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void unblockableEffectExpiresAfterTheTurn() {
        harness.addToBattlefield(player1, new TheEleventhDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.passUntilWithNoAttackers(player2,
                com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(target.isCantBeBlocked()).isFalse();
    }
}

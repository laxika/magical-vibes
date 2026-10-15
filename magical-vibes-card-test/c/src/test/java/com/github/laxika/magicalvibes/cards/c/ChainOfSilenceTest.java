package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KamahlFistOfKrosa;
import com.github.laxika.magicalvibes.cards.s.Sparksmith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfSilence.class, ElvishWarrior.class, Island.class, KamahlFistOfKrosa.class, Sparksmith.class})
class ChainOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt by the target creature this turn")
    void preventsAllDamageByTargetCreature() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(target.getId());
    }

    @Test
    @DisplayName("Offers the target creature's controller the land sacrifice")
    void offersSacrificeToTargetController() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
    }

    @Test
    @DisplayName("Sacrificing a land offers the target controller the copy")
    void sacrificingLandOffersCopy() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the land and creates no copy")
    void decliningSacrificeCreatesNoCopy() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("No land available means the target controller creates no copy")
    void noLandMeansNoCopy() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(target.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copy can be retargeted to a creature controlled by another player")
    void copiedSpellMayBeRetargetedToAnotherController() {
        Permanent originalTarget = addCreatureReady(player2, new ElvishWarrior());
        Permanent newTarget = addCreatureReady(player1, new ElvishWarrior());
        Permanent originalControllerLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent newTargetControllerLand = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, originalTarget.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, originalControllerLand.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId());
        harness.handlePermanentChosen(player2, newTarget.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.permanentsPreventedFromDealingDamage)
                .contains(originalTarget.getId(), newTarget.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(newTargetControllerLand.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copy that is not retargeted keeps its original target")
    void decliningCopyRetargetKeepsOriginalTarget() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(target.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(target.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Sacrificing the targeted animated land still permits copying and retargeting")
    void sacrificingTargetedAnimatedLandStillPermitsCopy() {
        harness.addToBattlefield(player2, new KamahlFistOfKrosa());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent newTarget = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, 0, null, land.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());

        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.permanentsPreventedFromDealingDamage).contains(newTarget.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player can sacrifice a land and decline to copy the spell")
    void sacrificingLandDoesNotRequireCopying() {
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.permanentsPreventedFromDealingDamage).contains(target.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Prevention stops combat damage to a player")
    void preventsCombatDamageToPlayer() {
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.handleMayAbilityChosen(player2, false);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
    }


    @Test
    @DisplayName("Prevention stops noncombat damage to both creatures and players")
    void preventsNoncombatDamageToCreaturesAndPlayers() {
        Permanent sparksmith = addCreatureReady(player2, new Sparksmith());
        Permanent target = addCreatureReady(player1, new ElvishWarrior());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, sparksmith.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

}

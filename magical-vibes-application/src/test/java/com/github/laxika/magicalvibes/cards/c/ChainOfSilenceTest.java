package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfSilence.class, ElvishWarrior.class, Island.class})
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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

}

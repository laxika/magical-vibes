package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RagingBattleMouse.class, Forest.class, GrizzlyBears.class})
class RagingBattleMouseTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell each turn does not get the reduction")
    void firstSpellIsNotReduced() {
        harness.addToBattlefield(player1, new RagingBattleMouse());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the second spell each turn costs {1} less")
    void onlySecondSpellIsReduced() {
        harness.addToBattlefield(player1, new RagingBattleMouse());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Celebration boosts a target creature you control")
    void celebrationBoostsTargetCreature() {
        castRagingBattleMouse();
        Permanent bear = castGrizzlyBears();

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Celebration does not trigger without two nonland permanents")
    void celebrationDoesNotTriggerWithoutTwoNonlandPermanents() {
        castRagingBattleMouse();
        harness.addToBattlefield(player1, new Forest());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Celebration only offers creatures you control")
    void celebrationOnlyOffersCreaturesYouControl() {
        castRagingBattleMouse();
        Permanent ownBear = castGrizzlyBears();
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(opposingBear.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not pay colored mana")
    void reductionDoesNotPayColoredMana() {
        castRagingBattleMouse();
        harness.setHand(player1, List.of(new RagingBattleMouse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A mouse on the stack does not reduce its own cost")
    void mouseDoesNotReduceItsOwnCost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new RagingBattleMouse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent does not receive the second-spell discount")
    void opponentsSecondSpellIsNotReduced() {
        harness.addToBattlefield(player1, new RagingBattleMouse());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land entering does not satisfy celebration")
    void actualLandEntryDoesNotSatisfyCelebration() {
        castRagingBattleMouse();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's nonland entry does not satisfy your celebration")
    void opponentsEntryDoesNotSatisfyCelebration() {
        castRagingBattleMouse();
        harness.enterBattlefieldAndReturn(player2, new RagingBattleMouse());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Celebration does not trigger during the opponent's combat")
    void celebrationDoesNotTriggerDuringOpponentsCombat() {
        castRagingBattleMouse();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new RagingBattleMouse());
        harness.forceActivePlayer(player2);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Celebration remembers permanents that have left the battlefield")
    void celebrationCountsPermanentsThatLeftBattlefield() {
        castRagingBattleMouse();
        Permanent mouse = findPermanent(player1, "Raging Battle Mouse");
        Permanent bear = castGrizzlyBears();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear);

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, mouse.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mouse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mouse)).isEqualTo(2);
    }

    @Test
    @DisplayName("A celebration ability resolves after its source leaves")
    void celebrationResolvesAfterSourceLeaves() {
        castRagingBattleMouse();
        Permanent mouse = findPermanent(player1, "Raging Battle Mouse");
        Permanent bear = castGrizzlyBears();
        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mouse);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Celebration's bonus expires at end of turn")
    void celebrationBonusExpiresAtEndOfTurn() {
        castRagingBattleMouse();
        Permanent bear = castGrizzlyBears();
        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    private void castRagingBattleMouse() {
        harness.setHand(player1, List.of(new RagingBattleMouse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    private void advanceToBeginningOfCombat() {
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}

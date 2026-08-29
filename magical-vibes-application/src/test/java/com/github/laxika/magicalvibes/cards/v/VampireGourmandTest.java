package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VampireGourmandTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent vampire = addCreatureReady(player1, new VampireGourmand());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(vampire.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature draws a card and makes Vampire Gourmand unblockable")
    void sacrificingAnotherCreatureDrawsAndMakesUnblockable() {
        Permanent vampire = addCreatureReady(player1, new VampireGourmand());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(vampire.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent vampire = addCreatureReady(player1, new VampireGourmand());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        Permanent currentVampire = findPermanent(player1, "Vampire Gourmand");
        assertThat(currentVampire.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("With no other creature, accepting the may does nothing")
    void noOtherCreatureDoesNothing() {
        Permanent vampire = addCreatureReady(player1, new VampireGourmand());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(vampire.isCantBeBlocked()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The unblockable effect expires at end of turn")
    void unblockableExpiresAtEndOfTurn() {
        Permanent vampire = addCreatureReady(player1, new VampireGourmand());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vampire.isCantBeBlocked()).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.isCantBeBlocked()).isFalse();
    }
}

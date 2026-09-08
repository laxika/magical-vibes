package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlhargTheRazeBoar.class, GrizzlyBears.class})
class IlhargTheRazeBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may put a creature from hand onto the battlefield tapped and attacking")
    void attackingPutsCreatureTappedAndAttacking() {
        Permanent ilharg = addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isAttackedThisTurn()).isTrue();
        assertThat(ilharg.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The creature put onto the battlefield returns to its owner's hand at the next end step")
    void attackingCreatureReturnsAtNextEndStep() {
        addCreatureReady(player1, new IlhargTheRazeBoar());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The death trigger may put Ilharg third from the top")
    void deathTriggerPutsIlhargThirdFromTop() {
        Card top = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card ilhargCard = ilharg.getCard();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ilharg));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), ilhargCard.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ilhargCard.getId()));
    }

    @Test
    @DisplayName("The exile trigger may put Ilharg third from the top")
    void exileTriggerPutsIlhargThirdFromTop() {
        Card top = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent ilharg = harness.addToBattlefieldAndReturn(player1, new IlhargTheRazeBoar());
        Card ilhargCard = ilharg.getCard();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ilharg));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), ilhargCard.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(ilhargCard.getId()));
    }
}

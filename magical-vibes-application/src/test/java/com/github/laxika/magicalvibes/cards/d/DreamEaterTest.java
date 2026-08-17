package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DreamEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils fewer than four cards, then may return an opposing nonland permanent")
    void surveilsAndReturnsOpponentPermanent() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("May decline leaves the opposing permanent on the battlefield")
    void decliningBounceLeavesPermanent() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3), List.of()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The reflexive target excludes lands and permanents controlled by the caster")
    void reflexiveTargetIsOpponentNonlandPermanent() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opposingLandId = harness.getPermanentId(player2, "Island");
        castDreamEater();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3), List.of()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds())
                .contains(opposingCreatureId)
                .doesNotContain(ownCreatureId, opposingLandId);
    }

    private void castDreamEater() {
        harness.setHand(player1, List.of(new DreamEater()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }
}

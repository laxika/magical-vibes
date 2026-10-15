package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExiledBoggart.class, Tarfire.class})
class ExiledBoggartTest extends BaseCardTest {

    @Test
    @DisplayName("When Exiled Boggart dies, its controller discards a card")
    void deathPromptsControllerDiscard() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        harness.setHand(player1, List.of(new ExiledBoggart()));

        // Player2 kills the Boggart with Tarfire.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID boggartId = harness.getPermanentId(player1, "Exiled Boggart");
        harness.castAndResolveInstant(player2, 0, boggartId);
        harness.passBothPriorities(); // Death trigger resolves

        // Controller must choose a card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled Boggart on the battlefield does not force a discard")
    void aliveDoesNotDiscard() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        harness.setHand(player1, List.of(new ExiledBoggart()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller chooses exactly one card to discard")
    void controllerChoosesOneCard() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        harness.setHand(player1, List.of(new ExiledBoggart(), new Tarfire()));
        harness.setHand(player2, List.of(new Tarfire(), new ExiledBoggart()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Exiled Boggart"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Tarfire");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Death with an empty hand resolves without a discard choice")
    void emptyHandDoesNotBlockResolution() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Tarfire(), new ExiledBoggart()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Exiled Boggart"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Exiled Boggart");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}

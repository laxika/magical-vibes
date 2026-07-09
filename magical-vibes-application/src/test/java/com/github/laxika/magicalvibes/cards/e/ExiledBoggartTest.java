package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExiledBoggartTest extends BaseCardTest {

    @Test
    @DisplayName("When Exiled Boggart dies, its controller discards a card")
    void deathPromptsControllerDiscard() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        // Player2 kills the 2/2 Boggart with Shock (2 damage).
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID boggartId = harness.getPermanentId(player1, "Exiled Boggart");
        harness.castInstant(player2, 0, boggartId);
        harness.passBothPriorities(); // Shock resolves → Boggart dies → death trigger
        harness.passBothPriorities(); // Death trigger resolves

        // Controller must choose a card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled Boggart on the battlefield does not force a discard")
    void aliveDoesNotDiscard() {
        harness.addToBattlefield(player1, new ExiledBoggart());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

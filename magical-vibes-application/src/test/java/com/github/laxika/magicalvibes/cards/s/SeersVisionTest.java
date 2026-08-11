package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeersVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Both players see each other's hands while Seer's Vision is on the battlefield")
    void bothHandsAreRevealed() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Sacrificing Seer's Vision lets its controller choose a card for the target to discard")
    void sacrificeAndDiscardChosenCard() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Seer's Vision");
        harness.assertInGraveyard(player1, "Seer's Vision");

        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot be activated outside its controller's main phase")
    void abilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Seer's Vision");
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoomWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life activates surveil 2")
    void paysLifeAndSurveilsTwo() {
        GameData gameData = harness.getGameData();
        Card topCard = new DoomWhisperer();
        Card secondCard = new DoomWhisperer();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        addCreatureReady(player1, new DoomWhisperer());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("Cannot activate Doom Whisperer's ability without 2 life")
    void cannotPayLife() {
        addCreatureReady(player1, new DoomWhisperer());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }
}

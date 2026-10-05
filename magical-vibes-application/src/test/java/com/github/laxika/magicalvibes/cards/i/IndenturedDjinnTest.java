package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AerialCaravan;
import com.github.laxika.magicalvibes.cards.c.ChamberedNautilus;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndenturedDjinn.class, CloudSprite.class, ChamberedNautilus.class, AerialCaravan.class})
class IndenturedDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Each other player may draw up to three cards when Indentured Djinn enters")
    void eachOtherPlayerMayDrawUpToThreeCards() {
        castDjinn();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The other player may choose to draw zero cards")
    void otherPlayerMayDrawZeroCards() {
        castDjinn();

        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("The other player may draw fewer than three cards")
    void otherPlayerMayDrawFewerThanThreeCards(int count) {
        castDjinn();

        harness.handleXValueChosen(player2, count);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(count);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3 - count);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The other player can decline to draw from an empty library")
    void otherPlayerMayDeclineWithEmptyLibrary() {
        castDjinn();
        harness.setLibrary(player2, List.of());

        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The draw choice excludes the controller when the other player controls the Djinn")
    void excludesOtherController() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CloudSprite(), new ChamberedNautilus(), new AerialCaravan()));

        harness.enterBattlefieldAndReturn(player2, new IndenturedDjinn());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDjinn() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CloudSprite(), new ChamberedNautilus(), new AerialCaravan()));

        harness.castFromHand(player1, new IndenturedDjinn(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

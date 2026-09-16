package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AerialCaravan;
import com.github.laxika.magicalvibes.cards.c.ChamberedNautilus;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private void castDjinn() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CloudSprite(), new ChamberedNautilus(), new AerialCaravan()));

        harness.castFromHand(player1, new IndenturedDjinn(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

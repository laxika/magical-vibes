package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WistfulThinking.class, Forest.class, GrizzlyBears.class})
class WistfulThinkingTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards, then discards four cards")
    void drawsTwoThenDiscardsFour() {
        harness.setHand(player1, List.of(new WistfulThinking()));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Wistful Thinking cannot target a permanent")
    void cannotTargetPermanent() {
        harness.setHand(player1, List.of(new WistfulThinking()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

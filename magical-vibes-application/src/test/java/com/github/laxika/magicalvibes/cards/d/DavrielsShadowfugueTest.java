package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArborealGrazer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DavrielsShadowfugue.class, ArborealGrazer.class})
class DavrielsShadowfugueTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards and loses 2 life")
    void targetPlayerDiscardsTwoAndLosesTwoLife() {
        ArborealGrazer first = new ArborealGrazer();
        ArborealGrazer second = new ArborealGrazer();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DavrielsShadowfugue(), first, second));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target with fewer than two cards discards their whole hand and still loses 2 life")
    void targetWithFewerCardsStillLosesLife() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ArborealGrazer()));
        harness.setHand(player1, List.of(new DavrielsShadowfugue()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.setHand(player1, List.of(new DavrielsShadowfugue()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        var permanent = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    @DisplayName("An empty-handed target still loses 2 life")
    void emptyHandedTargetStillLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DavrielsShadowfugue()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Davriel's Shadowfugue");
    }

    @Test
    @DisplayName("The target chooses exactly two cards from a larger hand before losing life")
    void targetChoosesTwoCardsFromLargerHand() {
        ArborealGrazer kept = new ArborealGrazer();
        ArborealGrazer firstDiscard = new ArborealGrazer();
        ArborealGrazer secondDiscard = new ArborealGrazer();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(kept, firstDiscard, secondDiscard));
        harness.setHand(player1, List.of(new DavrielsShadowfugue()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 1);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstDiscard.getId(), secondDiscard.getId());
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Davriel's Shadowfugue");
    }
}

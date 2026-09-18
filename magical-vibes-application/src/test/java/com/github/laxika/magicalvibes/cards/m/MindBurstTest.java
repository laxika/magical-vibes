package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindBurst.class, Forest.class, Island.class, DuskImp.class})
class MindBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards one plus the Mind Bursts in all graveyards")
    void discardsBasedOnMindBurstsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new MindBurst()));
        harness.setGraveyard(player2, List.of(new MindBurst(), new DuskImp()));
        harness.setHand(player1, List.of(new MindBurst()));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Island(), new DuskImp())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mind Burst", "Dusk Imp", "Forest", "Island", "Dusk Imp");
    }

    @Test
    @DisplayName("A target player with no Mind Bursts in graveyards discards one card")
    void discardsOneWhenNoMindBurstsAreInGraveyards() {
        harness.setHand(player1, List.of(new MindBurst()));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Island())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discards the target's entire hand when it has fewer cards than required")
    void discardsEntireHandWhenTargetHasFewerCardsThanRequired() {
        harness.setGraveyard(player1, List.of(new MindBurst(), new MindBurst()));
        harness.setHand(player1, List.of(new MindBurst()));
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Dusk Imp", "Dusk Imp");
    }
}

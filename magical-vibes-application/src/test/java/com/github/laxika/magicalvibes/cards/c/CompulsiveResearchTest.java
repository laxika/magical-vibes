package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompulsiveResearch.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class})
class CompulsiveResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws three cards and may stop after discarding a land")
    void targetPlayerDrawsThreeAndMayStopAfterDiscardingLand() {
        harness.setHand(player1, List.of(new CompulsiveResearch()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player2, List.of(new Island(), new Mountain(), new GrizzlyBears()));
        addCompulsiveResearchMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);

        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target player discards two cards when no land is discarded")
    void targetPlayerDiscardsTwoCardsWithoutLand() {
        harness.setHand(player1, List.of(new CompulsiveResearch()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCompulsiveResearchMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompulsiveResearch()));
        addCompulsiveResearchMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only target players");
    }

    private void addCompulsiveResearchMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

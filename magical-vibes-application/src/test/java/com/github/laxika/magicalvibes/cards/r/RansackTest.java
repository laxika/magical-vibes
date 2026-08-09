package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RansackTest extends BaseCardTest {

    private void castRansack() {
        harness.setHand(player1, List.of(new Ransack()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller splits and orders the target player's top five cards")
    void splitsAndOrdersTargetLibrary() {
        Card c0 = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Card c1 = new com.github.laxika.magicalvibes.cards.s.Shock();
        Card c2 = new com.github.laxika.magicalvibes.cards.p.Plains();
        Card c3 = new com.github.laxika.magicalvibes.cards.l.LlanowarElves();
        Card c4 = new com.github.laxika.magicalvibes.cards.h.HillGiant();
        Card c5 = new com.github.laxika.magicalvibes.cards.s.Shock();
        harness.setLibrary(player2, List.of(c0, c1, c2, c3, c4, c5));

        castRansack();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(c0, c1, c2, c3, c4);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(4, 1), List.of(3, 0, 2)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(c4, c1, c5, c3, c0, c2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short target library moves all available cards through the split")
    void handlesShortTargetLibrary() {
        Card c0 = new com.github.laxika.magicalvibes.cards.s.Shock();
        Card c1 = new com.github.laxika.magicalvibes.cards.p.Plains();
        harness.setLibrary(player2, List.of(c0, c1));

        castRansack();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(c0, c1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(c1, c0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

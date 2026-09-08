package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoBlankTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards, then their graveyard is exiled")
    void discardsTwoThenExilesTargetGraveyard() {
        harness.setHand(player2, List.of(new Forest(), new SerraAngel(), new LightningBolt()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new GoBlank()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Shock", "Forest", "Serra Angel");
        harness.assertInGraveyard(player1, "Go Blank");
    }

    @Test
    @DisplayName("Exiles the target player's graveyard even when they have no cards to discard")
    void emptyHandStillExilesGraveyard() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new GoBlank()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Shock");
    }
}

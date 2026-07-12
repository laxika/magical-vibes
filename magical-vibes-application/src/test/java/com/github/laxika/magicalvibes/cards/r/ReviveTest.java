package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviveTest extends BaseCardTest {

    @Test
    @DisplayName("Revive returns target green card from graveyard to hand")
    void returnsTargetGreenCardFromGraveyardToHand() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, green.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(green.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(green.getId()));
    }

    @Test
    @DisplayName("Revive cannot target a non-green card in graveyard")
    void cannotTargetNonGreenCard() {
        Card red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, red.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revive cannot target a green card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card green = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(green));
        harness.setHand(player1, List.of(new Revive()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, green.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}

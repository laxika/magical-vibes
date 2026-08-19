package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarOfWillTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Avatar of Will for {U}{U} when every opponent has cards in hand")
    void cannotCastWithCardsInOpponentsHands() {
        harness.setHand(player1, List.of(new AvatarOfWill()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Will for {U}{U} when an opponent has no cards in hand")
    void canCastWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new AvatarOfWill()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }
}

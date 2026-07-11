package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiantsIreTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4); // {3}{R}
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 4 damage to the target player")
    void dealsFourDamage() {
        int before = gd.getLife(player2.getId());
        cast();
        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 4);
    }

    @Test
    @DisplayName("Draws a card if you control a Giant")
    void drawsWithGiant() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a Giant")
    void noDrawWithoutGiant() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

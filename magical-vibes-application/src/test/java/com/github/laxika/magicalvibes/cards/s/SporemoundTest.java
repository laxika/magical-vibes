package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporemoundTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall — playing a land creates a 1/1 Saproling token")
    void landfallCreatesSaproling() {
        harness.addToBattlefield(player1, new Sporemound());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .findFirst()
                .orElseThrow();
        assertThat(saproling.getEffectivePower()).isEqualTo(1);
        assertThat(saproling.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent playing a land does not create a Saproling")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Sporemound());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .count()).isZero();
    }
}

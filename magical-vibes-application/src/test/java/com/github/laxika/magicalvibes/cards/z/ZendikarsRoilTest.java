package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZendikarsRoilTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall — playing a land creates a 2/2 Elemental token")
    void landfallCreatesElemental() {
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent playing a land does not create an Elemental")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZendikarsRoil());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elemental"))
                .count()).isZero();
    }
}

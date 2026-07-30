package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainsCallTest extends BaseCardTest {

    private long soldierTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Resolving Captain's Call creates three 1/1 white Soldier tokens under its controller")
    void createsThreeSoldierTokens() {
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isEqualTo(3);
        assertThat(soldierTokenCount(player2)).isZero();

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .forEach(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                });
    }
}

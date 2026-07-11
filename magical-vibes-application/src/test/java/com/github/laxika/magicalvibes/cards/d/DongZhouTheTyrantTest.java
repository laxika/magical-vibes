package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DongZhouTheTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an opponent's creature deal its power to that opponent")
    void opponentTakesPowerDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB

        // Hill Giant is 3/3 -> opponent loses 3, creature is unharmed (damage went to the player).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant") && p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("A 0-power creature deals no damage to its controller")
    void zeroPowerDealsNoDamage() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID ownGiant = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownGiant, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}

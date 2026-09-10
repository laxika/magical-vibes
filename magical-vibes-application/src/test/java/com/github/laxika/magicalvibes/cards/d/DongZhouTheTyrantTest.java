package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShuDefender;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DongZhouTheTyrant.class, ShuDefender.class, WallOfVines.class})
class DongZhouTheTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an opponent's creature deal its power to that opponent")
    void opponentTakesPowerDamage() {
        harness.addToBattlefield(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Shu Defender");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB

        // Shu Defender is 2/2 -> opponent loses 2, creature is unharmed (damage went to the player).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(findPermanent(player2, "Shu Defender").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A 0-power creature deals no damage to its controller")
    void zeroPowerDealsNoDamage() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing if the targeted creature leaves before the ability resolves")
    void doesNothingIfTargetLeavesBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID ownDefender = harness.getPermanentId(player1, "Shu Defender");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownDefender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}

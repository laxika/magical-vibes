package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AeronautCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Soldier you control")
    void etbPutsCounterOnAnotherSoldierYouControl() {
        harness.addToBattlefield(player1, new YotianSoldier());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID soldierId = harness.getPermanentId(player1, "Yotian Soldier");
        gs.playCard(gd, player1, 0, 0, soldierId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(soldierId))
                .findFirst().orElseThrow();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not target a non-Soldier creature you control")
    void etbDoesNotTargetNonSoldier() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aeronaut Cavalry");
        assertThat(gd.stack).isEmpty();
    }
}

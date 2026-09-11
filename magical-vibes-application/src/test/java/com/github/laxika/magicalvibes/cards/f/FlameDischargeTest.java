package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameDischarge.class, AirElemental.class, GrizzlyBears.class})
class FlameDischargeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage without a modified creature")
    void dealsXDamageWithoutModifiedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals X plus 2 damage when a modified creature was controlled as cast")
    void dealsXPlusTwoDamageWithModifiedCreature() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castFlameDischarge(target);

        gd.playerBattlefields.get(player1.getId()).remove(modifiedCreature);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void castFlameDischarge(Permanent target) {
        harness.setHand(player1, List.of(new FlameDischarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
    }
}

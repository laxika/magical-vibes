package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayaVeneratedFiremage.class, Shock.class})
class JayaVeneratedFiremageTest extends BaseCardTest {

    @Test
    @DisplayName("Jaya's -2 deals 2 damage to any target and removes two loyalty")
    void minusTwoDealsTwoDamage() {
        Permanent jaya = addReadyJaya(player1, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Jaya increases damage from another red source you control")
    void boostsAnotherRedSource() {
        addReadyJaya(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyJaya(Player player, int loyalty) {
        Permanent jaya = new Permanent(new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, loyalty);
        jaya.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jaya);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jaya;
    }
}

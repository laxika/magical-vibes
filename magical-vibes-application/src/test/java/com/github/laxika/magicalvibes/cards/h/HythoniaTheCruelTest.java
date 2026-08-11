package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.XathridGorgon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HythoniaTheCruelTest extends BaseCardTest {

    @Test
    @DisplayName("When Hythonia becomes monstrous, it destroys all non-Gorgon creatures")
    void becomingMonstrousDestroysNonGorgonCreatures() {
        Permanent hythonia = addReadyHythonia();
        Permanent friendlyBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hythonia.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hythonia).doesNotContain(friendlyBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon).doesNotContain(opposingBear);
    }

    @Test
    @DisplayName("Hythonia's monstrosity ability cannot be activated after it becomes monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyHythonia();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyHythonia() {
        Permanent hythonia = harness.addToBattlefieldAndReturn(player1, new HythoniaTheCruel());
        hythonia.setSummoningSick(false);
        return hythonia;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}

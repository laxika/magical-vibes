package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauntlessRiverMarshal.class, Island.class, RuneclawBear.class})
class DauntlessRiverMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an Island")
    void getsBoostWithIsland() {
        Permanent marshal = addCreatureReady(player1, new DauntlessRiverMarshal());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get +1/+1 without an Island")
    void noBoostWithoutIsland() {
        Permanent marshal = addCreatureReady(player1, new DauntlessRiverMarshal());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps target creature for {3}{U}")
    void tapsTargetCreature() {
        addCreatureReady(player1, new DauntlessRiverMarshal());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void opponentsIslandDoesNotGiveBoost() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new DauntlessRiverMarshal());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(1);
    }

    @Test
    void boostIsNotMultipliedAndEndsWhenLastIslandLeaves() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new DauntlessRiverMarshal());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickWithoutIsland() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new DauntlessRiverMarshal());
        marshal.setSummoningSick(true);
        marshal.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(marshal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canTargetItselfWithoutTappingAsCost() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new DauntlessRiverMarshal());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, marshal.getId());
        assertThat(marshal.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(marshal.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DauntlessRiverMarshal());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(island.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new DauntlessRiverMarshal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterMarshalLeavesBattlefield() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new DauntlessRiverMarshal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(marshal);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

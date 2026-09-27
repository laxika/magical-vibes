package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonBlood.class, AlphaMyr.class, WeldingJar.class})
class DragonBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three mana and taps to put a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dragonBlood.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void putsCounterOnOpponentsCreature() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent opponentCreature = addCreatureReady(player2, new AlphaMyr());

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dragonBlood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without three mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dragonBlood.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(dragonBlood.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        dragonBlood.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing if the target creature leaves before resolution")
    void fizzlesIfTargetCreatureLeavesBeforeResolution() {
        Permanent dragonBlood = harness.addToBattlefieldAndReturn(player1, new DragonBlood());
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dragonBlood.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.m.MazesEnd;
import com.github.laxika.magicalvibes.cards.p.PossibilityStorm;
import com.github.laxika.magicalvibes.cards.s.SimicCluestone;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VorelOfTheHullClade.class, KraulWarrior.class, MazesEnd.class, PossibilityStorm.class, SimicCluestone.class})
class VorelOfTheHullCladeTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles +1/+1 counters on a target creature and taps the source")
    void doublesCountersOnCreature() {
        Permanent vorel = addVorel(player1);
        Permanent bears = addCreatureReady(player1, new KraulWarrior());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(vorel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Doubles every kind of counter on the target")
    void doublesEachKindOfCounter() {
        addVorel(player1);
        Permanent bears = addCreatureReady(player1, new KraulWarrior());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Can target an artifact an opponent controls")
    void doublesCountersOnOpponentArtifact() {
        addVorel(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SimicCluestone());
        artifact.setCounterCount(CounterType.CHARGE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addVorel(player1);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PossibilityStorm());
        enchantment.setCounterCount(CounterType.CHARGE, 2);
        prepareTurn();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchantment.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing when the target has no counters")
    void noOpWithoutCounters() {
        addVorel(player1);
        Permanent bears = addCreatureReady(player1, new KraulWarrior());
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    void doublesCountersOnLand() {
        addVorel(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MazesEnd());
        land.setCounterCount(CounterType.CHARGE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    void canDoubleItsOwnCounters() {
        Permanent vorel = addVorel(player1);
        vorel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, vorel.getId());
        harness.passBothPriorities();

        assertThat(vorel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(vorel.isTapped()).isTrue();
    }

    @Test
    void doublesCounterCountsPresentAtResolution() {
        addVorel(player1);
        Permanent creature = addCreatureReady(player1, new KraulWarrior());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void abilityStillResolvesAfterVorelLeavesBattlefield() {
        Permanent vorel = addVorel(player1);
        Permanent creature = addCreatureReady(player1, new KraulWarrior());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vorel);
        gd.playerGraveyards.get(player1.getId()).add(vorel.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent vorel = harness.addToBattlefieldAndReturn(player1, new VorelOfTheHullClade());
        vorel.setSummoningSick(true);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vorel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vorel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent vorel = addVorel(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vorel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vorel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleCountersAfterTargetLeavesBattlefield() {
        addVorel(player1);
        Permanent creature = addCreatureReady(player2, new KraulWarrior());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addVorel(Player player) {
        return addCreatureReady(player, new VorelOfTheHullClade());
    }
}

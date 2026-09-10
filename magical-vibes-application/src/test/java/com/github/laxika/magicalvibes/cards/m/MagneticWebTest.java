package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagneticWeb.class, MetallicSliver.class})
class MagneticWebTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T} puts a magnet counter on target creature and taps the Web")
    void abilityPutsMagnetCounter() {
        Permanent web = harness.addToBattlefieldAndReturn(player1, new MagneticWeb());
        Permanent bears = addCreatureReady(player1, new MetallicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MAGNET)).isEqualTo(1);
        assertThat(web.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can put a magnet counter on an opponent's creature")
    void abilityCanTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent bears = addCreatureReady(player2, new MetallicSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MAGNET)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreaturePermanent() {
        Permanent web = harness.addToBattlefieldAndReturn(player1, new MagneticWeb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, web.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(web.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A magnet-counter creature attacking forces the other magnet-counter creatures to attack")
    void magnetBearersMustAttackTogether() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent first = addCreatureReady(player1, new MetallicSliver());
        Permanent second = addCreatureReady(player1, new MetallicSliver());
        first.setCounterCount(CounterType.MAGNET, 1);
        second.setCounterCount(CounterType.MAGNET, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must also attack");
    }

    @Test
    @DisplayName("All magnet-counter creatures attacking together is a legal declaration")
    void magnetBearersAttackingTogetherIsLegal() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent first = addCreatureReady(player1, new MetallicSliver());
        Permanent second = addCreatureReady(player1, new MetallicSliver());
        first.setCounterCount(CounterType.MAGNET, 1);
        second.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(1, 2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature without a magnet counter is not dragged into the attack")
    void creatureWithoutCounterIsFree() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent withCounter = addCreatureReady(player1, new MetallicSliver());
        addCreatureReady(player1, new MetallicSliver());
        withCounter.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Attacking only with creatures without magnet counters is legal")
    void nonBearerAttackDoesNotForceBearers() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent withCounter = addCreatureReady(player1, new MetallicSliver());
        addCreatureReady(player1, new MetallicSliver());
        withCounter.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped magnet-counter creature does not make the declaration illegal")
    void tappedBearerIsNotForced() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        Permanent tapped = addCreatureReady(player1, new MetallicSliver());
        attacker.setCounterCount(CounterType.MAGNET, 1);
        tapped.setCounterCount(CounterType.MAGNET, 1);
        tapped.tap();

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("When a magnet-counter creature attacks, the defender's magnet-counter creatures must block it")
    void magnetBearersMustBlockTheAttacker() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        Permanent blocker = addCreatureReady(player2, new MetallicSliver());
        attacker.setCounterCount(CounterType.MAGNET, 1);
        blocker.setCounterCount(CounterType.MAGNET, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());

        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("A defender's creature without a magnet counter is under no blocking requirement")
    void defenderWithoutCounterNeedNotBlock() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        Permanent blocker = addCreatureReady(player2, new MetallicSliver());
        attacker.setCounterCount(CounterType.MAGNET, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(blocker.getMustBlockIds()).isEmpty();

        prepareDeclareBlockers(player1);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped magnet-counter creature is not required to block")
    void tappedBearerNeedNotBlock() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        Permanent blocker = addCreatureReady(player2, new MetallicSliver());
        attacker.setCounterCount(CounterType.MAGNET, 1);
        blocker.setCounterCount(CounterType.MAGNET, 1);
        blocker.tap();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        prepareDeclareBlockers(player1);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An attacker without a magnet counter does not trigger the blocking requirement")
    void attackerWithoutCounterDoesNotTrigger() {
        harness.addToBattlefield(player1, new MagneticWeb());
        addCreatureReady(player1, new MetallicSliver());
        Permanent blocker = addCreatureReady(player2, new MetallicSliver());
        blocker.setCounterCount(CounterType.MAGNET, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @CardUsed(MasterOfCruelties.class)
    @DisplayName("A magnet-counter creature that can only attack alone is not forced to join")
    void canOnlyAttackAloneBearerIsNotForced() {
        harness.addToBattlefield(player1, new MagneticWeb());
        Permanent master = addCreatureReady(player1, new MasterOfCruelties());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        master.setCounterCount(CounterType.MAGNET, 1);
        attacker.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(2))).doesNotThrowAnyException();
    }
}

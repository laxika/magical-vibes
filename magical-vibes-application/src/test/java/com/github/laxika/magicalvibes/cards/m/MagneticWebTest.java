package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagneticWebTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T} puts a magnet counter on target creature and taps the Web")
    void abilityPutsMagnetCounter() {
        Permanent web = addWeb(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MAGNET)).isEqualTo(1);
        assertThat(web.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can put a magnet counter on an opponent's creature")
    void abilityCanTargetOpponentCreature() {
        addWeb(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MAGNET)).isEqualTo(1);
    }

    @Test
    @DisplayName("A magnet-counter creature attacking forces the other magnet-counter creatures to attack")
    void magnetBearersMustAttackTogether() {
        addWeb(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.MAGNET, 1);
        second.setCounterCount(CounterType.MAGNET, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must also attack");
    }

    @Test
    @DisplayName("All magnet-counter creatures attacking together is a legal declaration")
    void magnetBearersAttackingTogetherIsLegal() {
        addWeb(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.MAGNET, 1);
        second.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(1, 2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature without a magnet counter is not dragged into the attack")
    void creatureWithoutCounterIsFree() {
        addWeb(player1);
        Permanent withCounter = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Attacking only with creatures without magnet counters is legal")
    void nonBearerAttackDoesNotForceBearers() {
        addWeb(player1);
        Permanent withCounter = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MAGNET, 1);

        assertThatCode(() -> declareAttackers(player1, List.of(2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped magnet-counter creature does not make the declaration illegal")
    void tappedBearerIsNotForced() {
        addWeb(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.MAGNET, 1);
        tapped.setCounterCount(CounterType.MAGNET, 1);
        tapped.tap();

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("When a magnet-counter creature attacks, the defender's magnet-counter creatures must block it")
    void magnetBearersMustBlockTheAttacker() {
        addWeb(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
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
        addWeb(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.MAGNET, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(blocker.getMustBlockIds()).isEmpty();

        prepareDeclareBlockers(player1);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An attacker without a magnet counter does not trigger the blocking requirement")
    void attackerWithoutCounterDoesNotTrigger() {
        addWeb(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.MAGNET, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    private Permanent addWeb(Player player) {
        Permanent perm = new Permanent(new MagneticWeb());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

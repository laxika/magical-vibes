package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnstableFooting;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NineLives.class, Shock.class, GrizzlyBears.class, ReturnToNature.class,
        UnstableFooting.class, Opalescence.class, DressDown.class})
class NineLivesTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage and gets one incarnation counter")
    void preventsNoncombatDamageAndGetsCounter() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage and gets one incarnation counter per attacker")
    void preventsCombatDamageAndGetsCounter() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles at nine incarnation counters and its leaves trigger causes a loss")
    void exilesAtNineCountersAndLosesWhenItLeaves() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        nineLives.setCounterCount(CounterType.INCARNATION, 8);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(9);

        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Unpreventable damage still adds an incarnation counter")
    void unpreventableDamageStillAddsCounter() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new UnstableFooting()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castKickedInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unpreventable damage adds a counter to every applicable copy")
    void unpreventableDamageAddsCounterToEachCopy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NineLives());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new UnstableFooting()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castKickedInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(first.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("The damaged player chooses between two copies before prevention")
    void damagedPlayerChoosesWhichCopyPreventsDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NineLives());
        first.setCounterCount(CounterType.INCARNATION, 8);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getCounterCount(CounterType.INCARNATION)).isEqualTo(8);
        assertThat(second.getCounterCount(CounterType.INCARNATION)).isZero();
    }

    @Test
    @DisplayName("Losing abilities disables prevention and counter placement")
    void losingAbilitiesDisablesPrevention() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new DressDown());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isZero();
    }

    @Test
    @DisplayName("Simultaneous attackers each add a counter even beyond nine")
    void simultaneousSourcesCanExceedNineCounters() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        nineLives.setCounterCount(CounterType.INCARNATION, 8);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(10);
        harness.assertOnBattlefield(player1, "Nine Lives");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Nine Lives");
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Destroying Nine Lives below nine counters still causes its controller to lose")
    void destructionCausesLossBelowNineCounters() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 1, nineLives.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nine Lives");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Nine Lives")
    void opponentCannotTargetNineLives() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, nineLives.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Nine Lives");
    }

    @Test
    @DisplayName("Nine Lives does not prevent damage to the other player")
    void doesNotPreventDamageToOpponent() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isZero();
    }

    @Test
    @DisplayName("Unpreventable damage at eight counters still leads to exile and loss")
    void ninthCounterFromUnpreventableDamageCausesLoss() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        nineLives.setCounterCount(CounterType.INCARNATION, 8);
        harness.setHand(player2, List.of(new UnstableFooting()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castKickedInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Nine Lives");
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Removing counters after the exile ability triggers does not stop exile")
    void removingCountersAfterTriggerDoesNotStopExile() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        nineLives.setCounterCount(CounterType.INCARNATION, 8);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        nineLives.setCounterCount(CounterType.INCARNATION, 0);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Nine Lives");
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}

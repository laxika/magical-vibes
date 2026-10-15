package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AshmouthHound;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({Lumberknot.class, GrizzlyBears.class, Shock.class, DeadWeight.class,
        AshmouthHound.class, BlasphemousAct.class})
class LumberknotTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally creature dies")
    void getsCounterWhenAllyCreatureDies() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill ally creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's +1/+1 counter trigger

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lumberknot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lumberknot)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature dies")
    void getsCounterWhenOpponentCreatureDies() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player2, new GrizzlyBears());
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's +1/+1 counter trigger

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple creatures die")
    void accumulatesCounters() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill first creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's trigger

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Add and kill second creature
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bears2Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bears2Id);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's trigger

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lumberknot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lumberknot)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Lumberknot")
    void opponentCannotTargetLumberknot() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DeadWeight()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, lumberknot.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Lumberknot triggers on its own death after its controller targets it")
    void triggersOnItsOwnDeath() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, lumberknot.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(lumberknot.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lumberknot sees every simultaneous death including its own")
    void triggersForEverySimultaneousDeath() {
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player1, new AshmouthHound());
        harness.addToBattlefield(player2, new AshmouthHound());
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getSourcePermanentId()).isEqualTo(lumberknot.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}

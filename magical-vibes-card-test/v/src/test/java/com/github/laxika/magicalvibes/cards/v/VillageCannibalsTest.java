package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RollingTemblor;
import com.github.laxika.magicalvibes.cards.s.SelflessCathar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({VillageCannibals.class, GrizzlyBears.class, Shock.class, UnrulyMob.class,
        RollingTemblor.class, SelflessCathar.class, Xenograft.class})
class VillageCannibalsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally Human creature dies")
    void getsCounterWhenAllyHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player1, new UnrulyMob()); // Human creature

        Permanent cannibals = findPermanent(player1, "Village Cannibals");
        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill ally Human creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player1, "Unruly Mob");
        harness.castAndResolveInstant(player2, 0, mobId);
        harness.passBothPriorities(); // Resolve Village Cannibals' +1/+1 counter trigger

        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cannibals)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cannibals)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's Human creature dies")
    void getsCounterWhenOpponentHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new UnrulyMob()); // Opponent's Human creature

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill opponent's Human creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player2, "Unruly Mob");
        harness.castAndResolveInstant(player1, 0, mobId);
        harness.passBothPriorities(); // Resolve Village Cannibals' +1/+1 counter trigger

        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT get a counter when a non-Human creature dies")
    void doesNotGetCounterWhenNonHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Human

        Permanent cannibals = findPermanent(player1, "Village Cannibals");
        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Kill non-Human creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        // No trigger should have been added for Village Cannibals
        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple Human creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new UnrulyMob());

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Kill first Human creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player2, "Unruly Mob");
        harness.castAndResolveInstant(player1, 0, mobId);
        harness.passBothPriorities(); // Resolve Village Cannibals' trigger

        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Add and kill second Human creature
        harness.addToBattlefield(player2, new UnrulyMob());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mob2Id = harness.getPermanentId(player2, "Unruly Mob");
        harness.castAndResolveInstant(player1, 0, mob2Id);
        harness.passBothPriorities(); // Resolve Village Cannibals' trigger

        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cannibals)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cannibals)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        harness.addToBattlefield(player1, new VillageCannibals());
        UUID cannibalsId = harness.getPermanentId(player1, "Village Cannibals");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, cannibalsId);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers separately for each other Human dying simultaneously with it")
    void triggersForOtherHumansDyingSimultaneously() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new SelflessCathar());
        harness.addToBattlefield(player2, new SelflessCathar());
        harness.setHand(player1, List.of(new RollingTemblor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Uses the Human type granted on the battlefield when a creature dies")
    void triggersForCreatureMadeHumanByXenograft() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new Xenograft());
        findPermanent(player2, "Xenograft").setChosenSubtype(CardSubtype.HUMAN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent cannibals = findPermanent(player1, "Village Cannibals");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(cannibals.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.p.PlagueSliver;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FungusSliver.class, PlagueSliver.class, BenalishCavalry.class, FledglingMawcor.class,
        SuddenShock.class})
class FungusSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Fungus Sliver gets a +1/+1 counter when it survives damage")
    void grantsAbilityToItself() {
        Permanent fungusSliver = addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new FledglingMawcor());

        ping(pinger, fungusSliver);

        assertThat(fungusSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fungus Sliver grants the ability to Slivers controlled by another player")
    void grantsAbilityToOpponentsSlivers() {
        addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new FledglingMawcor());
        Permanent sliver = addCreatureReady(player2, new PlagueSliver());

        ping(pinger, sliver);

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Fungus Sliver grants a separate damage trigger")
    void multipleFungusSliversGrantMultipleTriggers() {
        addCreatureReady(player1, new FungusSliver());
        addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new FledglingMawcor());
        Permanent sliver = addCreatureReady(player2, new PlagueSliver());

        ping(pinger, sliver);

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The granted damage trigger resolves for a surviving Sliver even if Fungus Sliver dies")
    void combatDamageTriggerIsSnapshottedBeforeSourceDies() {
        addCreatureReady(player1, new FungusSliver());
        Permanent blocker = addCreatureReady(player2, new PlagueSliver());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fungus Sliver");
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fungus Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new FledglingMawcor());
        Permanent bears = addCreatureReady(player2, new BenalishCavalry());

        ping(pinger, bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Sliver that dies from damage does not get a counter")
    void lethalDamageDoesNotPutCounterOnSliver() {
        Permanent fungusSliver = addCreatureReady(player1, new FungusSliver());
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, fungusSliver.getId());
        resolveAllTriggers();

        assertThat(fungusSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fungus Sliver");
    }

    @Test
    @DisplayName("A single event dealing two damage gives a Sliver only one counter")
    void damageAmountDoesNotMultiplyCounters() {
        addCreatureReady(player1, new FungusSliver());
        Permanent sliver = addCreatureReady(player2, new PlagueSliver());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, sliver.getId());
        resolveAllTriggers();

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Plague Sliver");
    }

    @Test
    @DisplayName("Separate damage events each give the same Sliver a counter")
    void separateDamageEventsGiveSeparateCounters() {
        Permanent sliver = addCreatureReady(player1, new FungusSliver());
        Permanent firstPinger = addCreatureReady(player1, new FledglingMawcor());
        Permanent secondPinger = addCreatureReady(player1, new FledglingMawcor());

        ping(firstPinger, sliver);
        ping(secondPinger, sliver);

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Fungus Sliver");
    }

    private void ping(Permanent pinger, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(pinger), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}

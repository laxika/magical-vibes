package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.p.PlagueSliver;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FungusSliver.class, BonescytheSliver.class, PlagueSliver.class, GrizzlyBears.class,
        ProdigalSorcerer.class, Shock.class})
class FungusSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Fungus Sliver gets a +1/+1 counter when it survives damage")
    void grantsAbilityToItself() {
        Permanent fungusSliver = addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());

        ping(pinger, fungusSliver);

        assertThat(fungusSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fungus Sliver grants the ability to Slivers controlled by another player")
    void grantsAbilityToOpponentsSlivers() {
        addCreatureReady(player1, new FungusSliver());
        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent sliver = addCreatureReady(player2, new BonescytheSliver());

        ping(pinger, sliver);

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted damage trigger resolves for a surviving Sliver even if Fungus Sliver dies")
    void combatDamageTriggerIsSnapshottedBeforeSourceDies() {
        Permanent fungusSliver = addCreatureReady(player1, new FungusSliver());
        Permanent blocker = addCreatureReady(player2, new PlagueSliver());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
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
        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        ping(pinger, bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Sliver that dies from damage does not get a counter")
    void lethalDamageDoesNotPutCounterOnSliver() {
        Permanent fungusSliver = addCreatureReady(player1, new FungusSliver());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, fungusSliver.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(fungusSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fungus Sliver");
    }

    private void ping(Permanent pinger, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(pinger), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}

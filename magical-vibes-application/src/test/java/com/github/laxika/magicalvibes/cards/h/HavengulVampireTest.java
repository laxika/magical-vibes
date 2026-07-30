package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HavengulVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter after dealing combat damage to a player")
    void getsCounterOnCombatDamageToPlayer() {
        Permanent vampire = addCreatureReady(player1, new HavengulVampire());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities(); // resolve the combat damage trigger

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature dies")
    void getsCounterWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new HavengulVampire());
        Permanent vampire = findPermanent(player1, "Havengul Vampire");
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Shock resolves, Grizzly Bears dies
        harness.passBothPriorities(); // death trigger resolves

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accumulates counters as multiple creatures die")
    void accumulatesCountersFromMultipleDeaths() {
        harness.addToBattlefield(player1, new HavengulVampire());
        Permanent vampire = findPermanent(player1, "Havengul Vampire");
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
    }
}

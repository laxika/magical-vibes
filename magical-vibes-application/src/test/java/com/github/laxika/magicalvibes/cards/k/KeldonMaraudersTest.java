package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonMarauders.class, LightningBolt.class, ElspethKnightErrant.class})
class KeldonMaraudersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two time counters and deals 1 damage to a player")
    void entersWithCountersAndDamagesPlayer() {
        harness.setHand(player1, List.of(new KeldonMarauders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent marauders = findPermanent(player1, "Keldon Marauders");
        assertThat(marauders.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Leaves the battlefield and deals 1 damage to a planeswalker")
    void leavesBattlefieldDamagesPlaneswalker() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        Permanent planeswalker = new Permanent(new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, marauders.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keldon Marauders");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes a time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(marauders.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(marauders);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keldon Marauders");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}

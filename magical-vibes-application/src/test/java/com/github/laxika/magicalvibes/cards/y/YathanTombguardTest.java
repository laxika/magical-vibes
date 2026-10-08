package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YathanTombguard.class, Forest.class, GrizzlyBears.class})
class YathanTombguardTest extends BaseCardTest {

    @Test
    @CardUsed({YathanTombguard.class, Forest.class})
    void tombguardWithNonPowerCounterTriggersForItsOwnDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = addCreatureReady(player1, new YathanTombguard());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({YathanTombguard.class, Forest.class})
    void eachCounteredCreatureTriggersEachTombguardSeparately() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new YathanTombguard());
            attacker.setCounterCount(CounterType.CHARGE, 2);
            attacker.setAttacking(true);
        }

        resolveCombat();
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({YathanTombguard.class, Forest.class})
    void opposingCounteredCreatureDoesNotTriggerYourTombguard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent defender = addCreatureReady(player1, new YathanTombguard());
        defender.tap();
        Permanent attacker = addCreatureReady(player2, new YathanTombguard());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void counteredCreatureDealsCombatDamageDrawsAndLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new YathanTombguard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void creatureWithoutCountersDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new YathanTombguard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}

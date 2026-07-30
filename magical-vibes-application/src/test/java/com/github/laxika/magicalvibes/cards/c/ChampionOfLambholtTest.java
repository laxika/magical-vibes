package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChampionOfLambholtTest extends BaseCardTest {

    private static final String DENIAL = "Creatures with power less than this creature's power can't block creatures you control";

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature its controller controls enters")
    void getsCounterWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new ChampionOfLambholt());
        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears
        harness.passBothPriorities(); // resolve Champion's triggered ability

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noCounterWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new ChampionOfLambholt());
        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A weaker creature can't block an attacker its controller's opponent controls alongside Champion")
    void weakerCreatureCannotBlock() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfLambholt());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears()); // 2/2, power 2 < 4
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 4/4

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(DENIAL);
    }

    @Test
    @DisplayName("A creature with power equal to Champion's power may still block")
    void equalPowerCreatureCanBlock() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfLambholt());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new SerraAngel()); // 4/4, power 4 is not less than 4
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 4/4

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("The restriction only covers Champion's controller's creatures")
    void restrictionDoesNotProtectOpponentsAttackers() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new ChampionOfLambholt());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 4/4

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }
}

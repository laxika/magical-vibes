package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfiniteAuthority.class, GiantSpider.class, GrizzlyBears.class, WallOfWood.class})
class InfiniteAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature becomes blocked by a creature with toughness 3 or less, that creature is destroyed and the enchanted creature gets a counter")
    void becomesBlockedByLowToughnessCreature() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When the enchanted creature blocks a creature with toughness 3 or less, that creature is destroyed at end of combat")
    void blocksLowToughnessCreature() {
        Permanent blocker = addCreatureReady(player1, new WallOfWood());
        addAuthorityAttachedTo(player1, blocker);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with toughness greater than 3 is not destroyed")
    void highToughnessCreatureSurvives() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addAuthorityAttachedTo(Player player, Permanent creature) {
        Permanent aura = new Permanent(new InfiniteAuthority());
        gd.playerBattlefields.get(player.getId()).add(aura);
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}

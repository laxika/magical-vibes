package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AetherflameWall;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BasalSliver;
import com.github.laxika.magicalvibes.cards.i.IcatianCrier;
import com.github.laxika.magicalvibes.cards.p.PlagueSliver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampiricSliver.class, BasalSliver.class, AshcoatBear.class,
        AetherflameWall.class, IcatianCrier.class, PlagueSliver.class})
class VampiricSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Vampiric Sliver gets a +1/+1 counter when a creature it damaged dies")
    void grantsAbilityToItself() {
        Permanent vampiricSliver = addCreatureReady(player1, new VampiricSliver());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(vampiricSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vampiric Sliver grants the ability to Slivers controlled by another player")
    void grantsAbilityToAllSlivers() {
        addCreatureReady(player1, new VampiricSliver());
        Permanent attacker = addCreatureReady(player2, new PlagueSliver());
        Permanent blocker = addCreatureReady(player1, new AshcoatBear());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not get Vampiric Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new VampiricSliver());
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Vampiric Sliver does not trigger when the damaged creature survives")
    void doesNotTriggerWhenDamagedCreatureSurvives() {
        Permanent vampiricSliver = addCreatureReady(player1, new VampiricSliver());
        Permanent wall = addCreatureReady(player2, new AetherflameWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(vampiricSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Sliver gets one trigger for each creature it damages that dies")
    void triggersIndependentlyForEachSliver() {
        Permanent vampiricSliver = addCreatureReady(player1, new VampiricSliver());
        Permanent basalSliver = addCreatureReady(player1, new BasalSliver());
        Permanent firstBlocker = addCreatureReady(player2, new IcatianCrier());
        Permanent secondBlocker = addCreatureReady(player2, new IcatianCrier());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
        assertThat(vampiricSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(basalSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineBestThereIs.class, GrizzlyBears.class, Shock.class, LlanowarElves.class, HillGiant.class})
class WolverineBestThereIsTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles all damage Wolverine deals, but not damage from another creature")
    void doublesOnlyWolverinesDamage() {
        addCreatureReady(player1, new WolverineBestThereIs());
        addCreatureReady(player1, new LlanowarElves());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on Wolverine at end step after it damages a creature")
    void growsAfterDamagingCreatureThatDies() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        declareAttackersAndPrepareBlockers(List.of(0));
        declareBlock(elves, wolverine);
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not grow at end step after dealing damage only to a player")
    void doesNotGrowWithoutDamagingCreature() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regeneration ability saves Wolverine from lethal combat damage")
    void regenerationSavesWolverine() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wolverine.getRegenerationShield()).isEqualTo(1);

        wolverine.setBlocking(true);
        wolverine.addBlockingTarget(0);
        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wolverine, Best There Is");
        assertThat(wolverine.isTapped()).isTrue();
        assertThat(wolverine.getRegenerationShield()).isZero();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}

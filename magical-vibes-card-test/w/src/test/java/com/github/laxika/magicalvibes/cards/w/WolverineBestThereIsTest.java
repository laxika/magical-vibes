package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineBestThereIs.class, LlanowarElves.class, HillGiant.class, PreyUpon.class})
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

        addCreatureReady(player2, new HillGiant());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Wolverine, Best There Is");
        assertThat(wolverine.isTapped()).isTrue();
        assertThat(wolverine.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Grows at the opponent's end step after dealing damage while blocking")
    void growsAtOpponentsEndStep() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        addCreatureReady(player2, new LlanowarElves());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles fight damage and grows after regenerating from the fight")
    void doublesNoncombatDamageAndGrowsAfterRegeneration() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.castAndResolveSorcery(player1, 0, List.of(wolverine.getId(), giant.getId()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Wolverine, Best There Is");
        assertThat(wolverine.isTapped()).isTrue();
        assertThat(wolverine.getRegenerationShield()).isZero();
        assertThat(wolverine.getMarkedDamage()).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}

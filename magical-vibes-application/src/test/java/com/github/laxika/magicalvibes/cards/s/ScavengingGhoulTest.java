package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengingGhoul.class, LightningBolt.class, ScatheZombies.class})
class ScavengingGhoulTest extends BaseCardTest {
    @Test
    void countsDeathsBeforeEntering() {
        Permanent victim = addCreatureReady(player2, new ScatheZombies());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);

        Permanent ghoul = addReadyGhoul(player1);
        advanceToEndStepAndResolve();

        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
    }

    private void advanceToEndStepAndResolve() {
        advanceToEndStepAndResolve(player1);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve the trigger
    }

    // ===== End-step trigger: corpse counters per creature death =====

    @Test
    @DisplayName("Gains a corpse counter at end step for each creature that died this turn")
    void gainsCorpseCountersForDeaths() {
        Permanent ghoul = new Permanent(new ScavengingGhoul());
        gd.playerBattlefields.get(player1.getId()).add(ghoul);

        // Three creatures died this turn across both players.
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 2, Integer::sum);

        advanceToEndStepAndResolve();

        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains no corpse counter at end step when no creature died this turn")
    void noCorpseCounterWhenNoDeath() {
        Permanent ghoul = new Permanent(new ScavengingGhoul());
        gd.playerBattlefields.get(player1.getId()).add(ghoul);

        advanceToEndStepAndResolve();

        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Activated ability: remove a corpse counter to regenerate =====

    @Test
    @DisplayName("Removing a corpse counter grants a regeneration shield")
    void removeCorpseCounterForRegeneration() {
        Permanent ghoul = addReadyGhoul(player1);
        ghoul.setCounterCount(CounterType.CORPSE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ghoul.getRegenerationShield()).isEqualTo(1);
        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesFromLethalDamage() {
        Permanent ghoul = addReadyGhoul(player1);
        ghoul.setCounterCount(CounterType.CORPSE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ghoul);
        assertThat(ghoul.getRegenerationShield()).isZero();
        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isZero();
    }

    @Test
    void gainsCountersAtOpponentsEndStep() {
        Permanent ghoul = addReadyGhoul(player1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 2);

        advanceToEndStepAndResolve(player2);

        assertThat(ghoul.getCounterCount(CounterType.CORPSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability without a corpse counter")
    void cannotActivateWithoutCorpseCounter() {
        Permanent ghoul = addReadyGhoul(player1);
        ghoul.setCounterCount(CounterType.CORPSE, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyGhoul(Player player) {
        return addCreatureReady(player, new ScavengingGhoul());
    }
}

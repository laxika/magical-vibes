package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JungleWurm.class, GiantMantis.class, Boomerang.class, WallOfRoots.class})
class JungleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("With a single blocker Jungle Wurm is unchanged")
    void oneBlockerGivesNoPenalty() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
        assertThat(wurm.getEffectivePower()).isEqualTo(5);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("With three blockers Jungle Wurm gets -2/-2 until end of turn")
    void threeBlockersGiveMinusTwo() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-2);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-2);
        assertThat(wurm.getEffectivePower()).isEqualTo(3);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("If unblocked Jungle Wurm is unchanged")
    void unblockedGivesNoPenalty() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The penalty counts blockers remaining when the trigger resolves")
    void removedBlockerIsNotCounted() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(wurm.getPowerModifier()).isEqualTo(-1);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Removing the only blocker before resolution does not give a bonus")
    void noRemainingBlockersGivesNoBonus() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The penalty persists after combat and expires at the end of the turn")
    void penaltyExpiresAtEndOfTurn() {
        Permanent wurm = addCreatureReady(player1, new JungleWurm());
        wurm.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfRoots());
        addCreatureReady(player2, new WallOfRoots());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(wurm.getPowerModifier()).isEqualTo(-1);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-1);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(wall.getId(), 4));
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(wurm.getPowerModifier()).isEqualTo(-1);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }
}

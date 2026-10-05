package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.SkyshroudArcher;
import com.github.laxika.magicalvibes.cards.s.SkeletonScavengers;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LowlandBasilisk.class, WallOfBlossoms.class, PreyUpon.class, SkyshroudArcher.class,
        SpinedWurm.class, SkeletonScavengers.class})
class LowlandBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature at end of combat")
    void combatDamageDestroysAtEndOfCombat() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new WallOfBlossoms());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Wall of Blossoms");
        assertThat(gd.stack).anyMatch(stackEntry ->
                stackEntry.getCard().getName().equals("Lowland Basilisk"));
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Wall of Blossoms");
        harness.assertOnBattlefield(player1, "Lowland Basilisk");
    }

    @Test
    @DisplayName("Noncombat damage also destroys the damaged creature at the next end of combat")
    void noncombatDamageDestroysAtNextEndOfCombat() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        Permanent target = addCreatureReady(player2, new WallOfBlossoms());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(basilisk.getId(), target.getId()));

        harness.assertOnBattlefield(player2, "Wall of Blossoms");
        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(target.getId()));

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wall of Blossoms");
    }

    @Test
    @DisplayName("Damage from another creature you control does not trigger the Basilisk")
    void damageFromAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new LowlandBasilisk());
        Permanent attacker = addCreatureReady(player1, new SkyshroudArcher());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfBlossoms());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.stack).noneMatch(stackEntry ->
                stackEntry.getCard().getName().equals("Lowland Basilisk"));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Wall of Blossoms");
    }

    @Test
    @DisplayName("The ability still triggers when the Basilisk dies from combat damage")
    void abilityTriggersEvenWhenBasiliskDiesInCombat() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertNotOnBattlefield(player1, "Lowland Basilisk");
        harness.assertOnBattlefield(player2, "Spined Wurm");
        assertThat(gd.stack).anyMatch(stackEntry ->
                stackEntry.getCard().getName().equals("Lowland Basilisk"));
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Spined Wurm");
    }

    @Test
    @DisplayName("Damage to a player does not trigger destruction")
    void playerDamageDoesNotTrigger() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        basilisk.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Lowland Basilisk");
    }

    @Test
    @DisplayName("Damage after combat waits until the next turn's end of combat")
    void postcombatDamageWaitsUntilNextCombat() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        Permanent target = addCreatureReady(player2, new WallOfBlossoms());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(basilisk.getId(), target.getId()));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player2, "Wall of Blossoms");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Wall of Blossoms");
    }

    @Test
    @DisplayName("The delayed destruction retains the Basilisk as source and its ability's controller")
    void delayedDestructionRetainsSourceAndController() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new WallOfBlossoms());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
            assertThat(entry.getSourcePermanentId()).isEqualTo(basilisk.getId());
            assertThat(entry.getCard()).isSameAs(basilisk.getCard());
        });
    }

    @Test
    @DisplayName("The damaged creature can regenerate from the delayed destruction")
    void damagedCreatureCanRegenerate() {
        Permanent basilisk = addCreatureReady(player1, new LowlandBasilisk());
        Permanent scavengers = addCreatureReady(player2, new SkeletonScavengers());
        scavengers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        basilisk.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Skeleton Scavengers");
        assertThat(scavengers.isTapped()).isTrue();
        assertThat(scavengers.getMarkedDamage()).isZero();
        assertThat(scavengers.getRegenerationShield()).isZero();
    }
}

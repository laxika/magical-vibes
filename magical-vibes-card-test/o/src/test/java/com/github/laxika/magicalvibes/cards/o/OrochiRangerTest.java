package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrochiRanger.class, MossKami.class, OrderOfTheSacredBell.class})
class OrochiRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent ranger = addCreatureReady(player1, new OrochiRanger());
        ranger.setAttacking(true);
        // 5/5 survives the Ranger's 2 damage, so the tap/untap lock is observable.
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);

        assertThat(mossKami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock any creature")
    void unblockedDamageDoesNotTapCreatures() {
        Permanent ranger = addCreatureReady(player1, new OrochiRanger());
        ranger.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isFalse();
        assertThat(mossKami.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Tap and untap restriction belong to one combat-damage trigger")
    void combatDamageCreatesOneAbility() {
        Permanent ranger = addCreatureReady(player1, new OrochiRanger());
        ranger.setAttacking(true);
        addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ranger also locks a creature it damages while blocking")
    void blockingRangerLocksAttackerAfterDying() {
        Permanent attacker = addCreatureReady(player1, new OrderOfTheSacredBell());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OrochiRanger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Orochi Ranger");
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The other player's untap step does not consume the restriction")
    void restrictionWaitsForDamagedCreaturesController() {
        Permanent ranger = addCreatureReady(player1, new OrochiRanger());
        ranger.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }
}

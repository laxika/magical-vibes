package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoltariTrooper.class, KnightOfDawn.class})
class SoltariTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent trooper = addCreatureReady(player1, new SoltariTrooper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() == trooper.getCard());
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking and the trigger resolves")
    void boostsOnAttack() {
        Permanent trooper = addCreatureReady(player1, new SoltariTrooper());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, trooper)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, trooper)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 wears off at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent trooper = addCreatureReady(player1, new SoltariTrooper());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, trooper)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, trooper)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, trooper)).isEqualTo(0);
    }

    @Test
    @DisplayName("The attack bonus waits for resolution and affects only the attacking Trooper")
    void attackBonusWaitsForResolutionAndAffectsOnlyItsSource() {
        Permanent attacker = addCreatureReady(player1, new SoltariTrooper());
        Permanent nonAttacker = addCreatureReady(player1, new SoltariTrooper());

        declareAttackers(player1, List.of(0));

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, nonAttacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonAttacker)).isZero();
    }

    @Test
    @DisplayName("Each attacking Trooper gets its own bonus")
    void multipleAttackersEachReceiveTheirOwnBonus() {
        Permanent first = addCreatureReady(player1, new SoltariTrooper());
        Permanent second = addCreatureReady(player1, new SoltariTrooper());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking Soltari Trooper")
    void shadowPreventsNonShadowCreatureFromBlocking() {
        Permanent trooper = addCreatureReady(player1, new SoltariTrooper());
        trooper.setAttacking(true);
        addCreatureReady(player2, new KnightOfDawn());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Soltari Trooper can block another creature with shadow")
    void shadowAllowsShadowCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new SoltariTrooper());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SoltariTrooper());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A Soltari Trooper cannot block a non-shadow creature")
    void shadowPreventsTrooperFromBlockingNonShadowCreature() {
        Permanent attacker = addCreatureReady(player1, new KnightOfDawn());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SoltariTrooper());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}

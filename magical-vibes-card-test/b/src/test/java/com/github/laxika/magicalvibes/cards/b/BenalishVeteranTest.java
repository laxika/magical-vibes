package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishVeteran.class})
class BenalishVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Benalish Veteran"));
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking and the trigger resolves")
    void boostsOnAttack() {
        Permanent veteran = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 wears off at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent veteran = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for resolution and affects only the attacking Veteran")
    void onlyAttackingVeteranGetsBoostAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new BenalishVeteran());
        Permanent idleVeteran = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, idleVeteran)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, idleVeteran)).isZero();
    }

    @Test
    @DisplayName("Each attacking Veteran gets its own boost")
    void multipleAttackersEachGetOneBoost() {
        Permanent first = addCreatureReady(player1, new BenalishVeteran());
        Permanent second = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Veteran controlled by the second player also gets its attack boost")
    void secondPlayersVeteranGetsBoost() {
        Permanent veteran = addCreatureReady(player2, new BenalishVeteran());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
    }
}

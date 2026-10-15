package com.github.laxika.magicalvibes.cards.b;

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
    @DisplayName("Attacking puts the ON_ATTACK trigger on the stack")
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

        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 wears off at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent veteran = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(veteran.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(veteran.getPowerModifier()).isEqualTo(0);
        assertThat(veteran.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for resolution and affects only the attacking Veteran")
    void onlyAttackingVeteranGetsBoostAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new BenalishVeteran());
        Permanent idleVeteran = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0));

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(idleVeteran.getPowerModifier()).isZero();
        assertThat(idleVeteran.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Veteran gets its own boost")
    void multipleAttackersEachGetOneBoost() {
        Permanent first = addCreatureReady(player1, new BenalishVeteran());
        Permanent second = addCreatureReady(player1, new BenalishVeteran());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Veteran controlled by the second player also gets its attack boost")
    void secondPlayersVeteranGetsBoost() {
        Permanent veteran = addCreatureReady(player2, new BenalishVeteran());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}

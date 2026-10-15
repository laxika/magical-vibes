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

@CardUsed({BrambleCreeper.class})
class BrambleCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new BrambleCreeper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Bramble Creeper"));
    }

    @Test
    @DisplayName("Gets +5/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent creeper = addCreatureReady(player1, new BrambleCreeper());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creeper.getPowerModifier()).isEqualTo(5);
        assertThat(creeper.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+5/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent creeper = addCreatureReady(player1, new BrambleCreeper());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creeper.getPowerModifier()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creeper.getPowerModifier()).isEqualTo(0);
        assertThat(creeper.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost applies only when the trigger resolves")
    void boostWaitsForResolution() {
        Permanent creeper = addCreatureReady(player1, new BrambleCreeper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(creeper.getPowerModifier()).isZero();

        resolveAllTriggers();

        assertThat(creeper.getPowerModifier()).isEqualTo(5);
        assertThat(creeper.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking copy boosts itself without boosting nonattacking copies")
    void attackingCopiesBoostOnlyThemselves() {
        Permanent first = addCreatureReady(player1, new BrambleCreeper());
        Permanent second = addCreatureReady(player1, new BrambleCreeper());
        Permanent nonattacker = addCreatureReady(player1, new BrambleCreeper());
        Permanent opposingCopy = addCreatureReady(player2, new BrambleCreeper());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(5);
        assertThat(second.getPowerModifier()).isEqualTo(5);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(opposingCopy.getPowerModifier()).isZero();
    }
}

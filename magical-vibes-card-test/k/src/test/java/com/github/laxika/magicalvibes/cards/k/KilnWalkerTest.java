package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KilnWalker.class})
class KilnWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Kiln Walker"));
    }

    @Test
    @DisplayName("Gets +3/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent walker = addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(walker.getPowerModifier()).isEqualTo(3);
        assertThat(walker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+3/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent walker = addCreatureReady(player1, new KilnWalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(walker.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(walker.getPowerModifier()).isEqualTo(0);
        assertThat(walker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for the trigger to resolve")
    void boostWaitsForResolution() {
        Permanent walker = addCreatureReady(player1, new KilnWalker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(walker.getPowerModifier()).isZero();
        assertThat(walker.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(walker.getPowerModifier()).isEqualTo(3);
        assertThat(walker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only the attacking copy receives its boost")
    void boostsOnlyAttackingCopy() {
        Permanent attacker = addCreatureReady(player1, new KilnWalker());
        Permanent nonattacker = addCreatureReady(player1, new KilnWalker());
        Permanent opponent = addCreatureReady(player2, new KilnWalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }
}

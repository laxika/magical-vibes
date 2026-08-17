package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinBanneretTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new GoblinBanneret());
        Permanent attackingLowerPowerCreature = addCreatureReady(player1, new Ornithopter());
        Permanent nonAttackingLowerPowerCreature = addCreatureReady(player1, new Ornithopter());
        Permanent attackingEqualPowerCreature = addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingLowerPowerCreature.getId());

        harness.handlePermanentChosen(player1, attackingLowerPowerCreature.getId());
        resolveAllTriggers();

        assertThat(attackingLowerPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingLowerPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attackingEqualPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activated ability gives Goblin Banneret +2/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(banneret.getPowerModifier()).isEqualTo(2);
        assertThat(banneret.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Activated ability boost wears off at end of turn")
    void activatedAbilityBoostResetsAtEndOfTurn() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(banneret.getPowerModifier()).isZero();
        assertThat(banneret.getToughnessModifier()).isZero();
    }
}

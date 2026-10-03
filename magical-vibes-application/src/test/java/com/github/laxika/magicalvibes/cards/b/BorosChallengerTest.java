package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosChallenger.class, HuntedWitness.class})
class BorosChallengerTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new BorosChallenger());
        Permanent attackingWizard = addCreatureReady(player1, new HuntedWitness());
        Permanent nonAttackingWizard = addCreatureReady(player1, new HuntedWitness());
        Permanent equalPowerCreature = addCreatureReady(player1, new BorosChallenger());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activated ability gives Boros Challenger +1/+1 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent challenger = addCreatureReady(player1, new BorosChallenger());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(challenger.getPowerModifier()).isEqualTo(1);
        assertThat(challenger.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(challenger.getPowerModifier()).isZero();
        assertThat(challenger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Mentor cannot target an attacking creature with equal power")
    void noTargetWhenAllAttackersHaveEqualPower() {
        Permanent first = addCreatureReady(player1, new BorosChallenger());
        Permanent second = addCreatureReady(player1, new BorosChallenger());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor rechecks the target's power when resolving")
    void mentorDoesNotAddCounterWhenTargetHasGrownToEqualPower() {
        addCreatureReady(player1, new BorosChallenger());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        witness.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability can be used repeatedly while tapped")
    void repeatedActivationsStackWhileTapped() {
        Permanent challenger = addCreatureReady(player1, new BorosChallenger());
        challenger.tap();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(challenger.getPowerModifier()).isEqualTo(2);
        assertThat(challenger.getToughnessModifier()).isEqualTo(2);
    }
}

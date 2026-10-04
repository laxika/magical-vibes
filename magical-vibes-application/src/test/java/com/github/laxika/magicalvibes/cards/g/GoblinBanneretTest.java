package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBanneret.class, Memnite.class, Ornithopter.class, HuntedWitness.class, JusticeStrike.class})
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

    @Test
    @DisplayName("Mentor has no legal target when all attacking creatures have equal power")
    void mentorCannotTargetEqualPowerAttackers() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(banneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Increasing Banneret's power before attacking enables mentor")
    void boostBeforeAttackingEnablesMentor() {
        addCreatureReady(player1, new GoblinBanneret());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor does not add a counter if its target grows to equal power")
    void mentorRechecksTargetPowerAtResolution() {
        addCreatureReady(player1, new GoblinBanneret());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, ornithopter.getId());
        ornithopter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor rechecks Banneret's current power when resolving")
    void mentorRechecksSourcePowerAtResolution() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, ornithopter.getId());
        banneret.setPowerModifier(-1);
        resolveAllTriggers();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses Banneret's last battlefield power after it dies")
    void mentorUsesLastKnownBoostedPowerAfterSourceDies() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, banneret.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(banneret);
        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Banneret can activate repeatedly while tapped and summoning sick")
    void repeatedActivationsStackWithoutTapOrHasteRequirement() {
        Permanent banneret = addCreatureReady(player1, new GoblinBanneret());
        banneret.setSummoningSick(true);
        banneret.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(banneret.getPowerModifier()).isEqualTo(4);
        assertThat(banneret.getToughnessModifier()).isZero();
        assertThat(banneret.isTapped()).isTrue();
    }
}

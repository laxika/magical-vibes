package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshalOfTheLost.class, GrizzlyBears.class})
class MarshalOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("When you attack, target creature gets +X/+X for the number of attacking creatures")
    void boostsTargetCreatureByNumberOfAttackers() {
        addCreatureReady(player1, new MarshalOfTheLost());
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker2)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MarshalOfTheLost());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when no creatures attack")
    void doesNotTriggerWithoutAttackers() {
        addCreatureReady(player1, new MarshalOfTheLost());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Marshal can attack alone and target itself")
    void canBoostItselfWhenAttackingAlone() {
        Permanent marshal = addCreatureReady(player1, new MarshalOfTheLost());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, marshal.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent attacking does not trigger Marshal")
    void doesNotTriggerForOpponentAttack() {
        Permanent marshal = addCreatureReady(player1, new MarshalOfTheLost());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
    }

    @Test
    @DisplayName("An attacker that leaves before resolution is excluded from X")
    void countsRemainingAttackersAtResolution() {
        addCreatureReady(player1, new MarshalOfTheLost());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, attacker));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability resolves with X zero when its sole attacker leaves")
    void resolvesAfterMarshalLeavesWithNoAttackersRemaining() {
        Permanent marshal = addCreatureReady(player1, new MarshalOfTheLost());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, marshal));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X stays fixed after resolution even if the attacker leaves")
    void boostDoesNotShrinkAfterResolution() {
        Permanent marshal = addCreatureReady(player1, new MarshalOfTheLost());
        Permanent target = addCreatureReady(player2, new MarshalOfTheLost());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, marshal));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}

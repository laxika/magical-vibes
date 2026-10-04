package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GryffwingCavalry.class, GrizzlyBears.class, HillGiant.class, WindDrake.class, Abrade.class})
class GryffwingCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} gives the targeted attacking creature flying")
    void payingManaGrantsFlying() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does not grant flying")
    void decliningPaymentDoesNotGrantFlying() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying ends at end of turn")
    void flyingEndsAtEndOfTurn() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A creature with flying is not a legal target")
    void creatureWithFlyingIsNotLegalTarget() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, drake.getId()))
                .isInstanceOf(IllegalStateException.class);
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Training puts a +1/+1 counter on Gryffwing Cavalry when attacking with a larger creature")
    void trainingTriggersWithGreaterPowerAttacker() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(giant);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonattacking creatures cannot be chosen when a legal attacker exists")
    void nonattackingCreatureIsNotLegalTarget() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        chooseAttackTriggerTarget(attacker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Training requires a greater-power attacker, not an equal attacker or larger nonattacker")
    void trainingRequiresGreaterPowerAttacker() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple larger attackers produce only one training counter")
    void trainingTriggersOnceWithMultipleLargerAttackers() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1, 2));
        chooseAttackTriggerTarget(giant);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking alone has no legal flying target and does not train")
    void attackingAloneHasNoLegalTarget() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Generic mana cannot replace the white mana in the payment")
    void paymentRequiresWhiteMana() {
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Training still resolves after the larger attacker dies, but its targeted flying ability does not")
    void trainingSurvivesDeathOfLargerAttacker() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(giant);
        harness.castInstant(player2, 0, 0, giant.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The flying ability can resolve after Gryffwing Cavalry dies")
    void flyingAbilitySurvivesSourceDeath() {
        Permanent cavalry = addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        chooseAttackTriggerTarget(bears);
        harness.castInstant(player2, 0, 0, cavalry.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gryffwing Cavalry");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A second flying trigger does not resolve after its target gains flying")
    void targetGainingFlyingMakesRemainingTriggerIllegal() {
        addCreatureReady(player1, new GryffwingCavalry());
        addCreatureReady(player1, new GryffwingCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0, 1, 2));
        chooseAttackTriggerTarget(bears);
        chooseAttackTriggerTarget(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseAttackTriggerTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
    }
}

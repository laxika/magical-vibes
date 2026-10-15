package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChromeHostHulk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnottvoldHermit.class, ChromeHostHulk.class, GrizzlyBears.class})
class GnottvoldHermitTest extends BaseCardTest {

    @Test
    void transformsWithPhyrexianManaAbility() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hermit.getCard().getName()).isEqualTo("Chrome Host Hulk");
        assertThat(hermit.isTransformed()).isTrue();
    }

    @Test
    void attackTriggerSetsAnotherCreatureToFiveFiveUntilEndOfTurn() {
        Permanent hulk = addCreatureReady(player1, new GnottvoldHermit());
        hulk.setCard(hulk.getOriginalCard().getBackFaceCard());
        hulk.setTransformed(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void attackTriggerCannotTargetTheHulkItself() {
        Permanent hulk = addCreatureReady(player1, new GnottvoldHermit());
        hulk.setCard(hulk.getOriginalCard().getBackFaceCard());
        hulk.setTransformed(true);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hulk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformsByPayingTwoLifeInsteadOfBlueMana() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 18);
        assertThat(hermit.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(hermit.isTransformed()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotTransformDuringCombat() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hermit.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hermit.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateTransformationAgainWhileAbilityIsOnStack() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        assertThat(hermit.isTransformed()).isTrue();
    }

    @Test
    void attackTriggerCanChooseNoTargetWhenAnotherCreatureExists() {
        addCreatureReady(player1, new ChromeHostHulk());
        Permanent other = addCreatureReady(player1, new GnottvoldHermit());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackTriggerCanTargetAnotherCreatureControlledByItsController() {
        addCreatureReady(player1, new ChromeHostHulk());
        Permanent other = addCreatureReady(player1, new GnottvoldHermit());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void settingBasePowerAndToughnessPreservesCounters() {
        addCreatureReady(player1, new ChromeHostHulk());
        Permanent other = addCreatureReady(player1, new GnottvoldHermit());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);

        gd.expireEndOfTurnFloatingEffects();
        other.resetModifiers();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
    }

    @Test
    void attackingWithoutAnotherCreatureNeedsNoTargetChoice() {
        addCreatureReady(player1, new ChromeHostHulk());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WaryThespian;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({MaraudingDreadship.class, WaryThespian.class})
class MaraudingDreadshipTest extends BaseCardTest {

    @Test
    void entersWithAnIncubatorTokenWithTwoCounters() {
        harness.setHand(player1, List.of(new MaraudingDreadship()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        assertThat(countPermanents(player2, "Incubator")).isZero();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.INCUBATOR);
    }

    @Test
    void crewTwoAnimatesTheDreadshipUntilEndOfTurn() {
        Permanent dreadship = addCreatureReady(player1, new MaraudingDreadship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WaryThespian());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadship.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(gqs.isCreature(gd, dreadship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadship)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dreadship)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(dreadship.isTapped()).isFalse();
    }

    @Test
    void incubatorTransformsForTwoManaAndKeepsItsCounters() {
        harness.enterBattlefieldAndReturn(player1, new MaraudingDreadship());
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
        assertThat(incubator.isTapped()).isFalse();
        assertThat(als.canAttack(gd, incubator, player1.getId())).isFalse();
    }

    @Test
    void incubatorCannotTransformWithOnlyOneMana() {
        harness.enterBattlefieldAndReturn(player1, new MaraudingDreadship());
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(incubator.isTransformed()).isFalse();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
    }

    @Test
    void twoPendingTransformActivationsDoNotTransformTheIncubatorBack() {
        harness.enterBattlefieldAndReturn(player1, new MaraudingDreadship());
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
    }

    @Test
    void newlyTransformedIncubatorCanCrewAndDreadshipCanAttackImmediately() {
        Permanent dreadship = harness.enterBattlefieldAndReturn(player1, new MaraudingDreadship());
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(als.canAttack(gd, dreadship, player1.getId())).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.activateAbility(player1, 0, null, null);
        assertThat(incubator.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dreadship)).isFalse();
        resolveAllTriggers();

        assertThat(als.canAttack(gd, dreadship, player1.getId())).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 16);
    }

    @Test
    void crewAnimationEndsWhenTheTurnEnds() {
        Permanent dreadship = addCreatureReady(player1, new MaraudingDreadship());
        harness.addToBattlefield(player1, new WaryThespian());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, dreadship)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, dreadship)).isFalse();
    }

    @Test
    void opponentsCreaturesCannotPayTheCrewCost() {
        Permanent dreadship = addCreatureReady(player1, new MaraudingDreadship());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WaryThespian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, dreadship)).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void tappedCreaturesCannotPayTheCrewCost() {
        Permanent dreadship = addCreatureReady(player1, new MaraudingDreadship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, dreadship)).isFalse();
    }
}

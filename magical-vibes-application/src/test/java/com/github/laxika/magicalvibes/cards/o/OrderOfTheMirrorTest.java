package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BolaSlinger;
import com.github.laxika.magicalvibes.cards.s.SaibaCryptomancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheMirror.class, OrderOfTheAlabasterHost.class, BolaSlinger.class, SaibaCryptomancer.class})
class OrderOfTheMirrorTest extends BaseCardTest {

    @Test
    void transformsByPayingWhiteMana() {
        Permanent order = transformOrder();

        assertThat(order.isTransformed()).isTrue();
        assertThat(order.getCard()).isInstanceOf(OrderOfTheAlabasterHost.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        addOrder();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addOrder();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void eachBlockingCreatureGetsMinusOneMinusOne() {
        Permanent order = transformOrder();
        order.setSummoningSick(false);
        addCreatureReady(player2, new BolaSlinger());
        addCreatureReady(player2, new BolaSlinger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).getPowerModifier()).isEqualTo(-1);
        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).getToughnessModifier()).isEqualTo(-1);
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).getPowerModifier()).isEqualTo(-1);
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void blockerPenaltyWearsOffAtEndOfTurn() {
        Permanent order = transformOrder();
        order.setSummoningSick(false);
        addCreatureReady(player2, new BolaSlinger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).get(0);
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    void whiteManaPaymentDoesNotCostLife() {
        Permanent order = transformOrder();

        assertThat(order.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTransformDuringOwnUpkeep() {
        addOrder();
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateTransformationWithAnAbilityOnTheStack() {
        Permanent order = addOrder();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();

        assertThat(order.isTransformed()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    void lifePaymentDoesNotReplaceTheGenericManaCost() {
        Permanent order = addOrder();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(order.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void frontFaceDoesNotPenalizeBlockers() {
        Permanent order = addOrder();
        order.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new BolaSlinger());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    void blockingCreatureWithHexproofStillDiesFromThePenalty() {
        Permanent order = transformOrder();
        order.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new SaibaCryptomancer());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Saiba Cryptomancer");
        harness.assertLife(player2, 20);
    }

    private Permanent addOrder() {
        return harness.addToBattlefieldAndReturn(player1, new OrderOfTheMirror());
    }

    private Permanent transformOrder() {
        Permanent order = addOrder();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return order;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EgoErasure;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrennAndSix;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladebackSliver.class, BonescytheSliver.class, GrizzlyBears.class, WrennAndSix.class, EgoErasure.class})
class BladebackSliverTest extends BaseCardTest {

    @Test
    void hellbentGrantsDamageAbilityToSliversYouControl() {
        Permanent bladeback = addCreatureReady(player1, new BladebackSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, indexOf(bladeback), null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(sliver), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void damageAbilityIsUnavailableWithCardsInHand() {
        addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityIsGrantedOnlyToSliversYouControl() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        assertThatThrownBy(() -> harness.activateAbility(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponentSliver), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityCannotTargetACreature() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityCanTargetAPlaneswalker() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new WrennAndSix());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageAbilityCanTargetItsController() {
        addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void damageAbilityIsNotGrantedToNonSliverCreatures() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(bear), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatingDamageAbilityTapsTheSliverAndPreventsAnotherActivation() {
        Permanent bladeback = addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(bladeback.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void summoningSickSliverCannotActivateDamageAbility() {
        harness.addToBattlefield(player1, new BladebackSliver());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    void emptyingHandMakesDamageAbilityAvailableImmediately() {
        addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of(new BladebackSliver()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player1, List.of());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void gainingACardRemovesAbilityButDoesNotStopAnActivatedAbility() {
        addCreatureReady(player1, new BladebackSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setHand(player1, List.of(new BladebackSliver()));

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sliver), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    void removingBladebackRemovesGrantedAbilityButDoesNotStopAnActivatedAbility() {
        Permanent bladeback = addCreatureReady(player1, new BladebackSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, indexOf(sliver), null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bladeback);
        gd.playerGraveyards.get(player1.getId()).add(bladeback.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        sliver.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sliver), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAbilityResolvesAfterItsSliverLeavesTheBattlefield() {
        Permanent bladeback = addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bladeback);
        gd.playerGraveyards.get(player1.getId()).add(bladeback.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void bladebackCannotUseGrantedAbilityAfterLosingSliverType() {
        addCreatureReady(player1, new BladebackSliver());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EgoErasure()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

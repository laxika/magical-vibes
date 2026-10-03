package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraNovicePyromancer.class, AirElemental.class, GreenwoodSentinel.class, Plains.class})
class ChandraNovicePyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 boosts only Elementals you control until end of turn")
    void plusOneBoostsControlledElementalsUntilEndOfTurn() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposingElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(elemental.getToughnessModifier()).isZero();
        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(opposingElemental.getPowerModifier()).isZero();

        Permanent laterElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        assertThat(laterElemental.getPowerModifier()).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(elemental.getPowerModifier()).isZero();
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("−1 adds two red mana and removes one loyalty")
    void minusOneAddsTwoRedMana() {
        Permanent chandra = addReadyChandra(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("−2 deals two damage to any target")
    void minusTwoDealsTwoDamageToPlayer() {
        Permanent chandra = addReadyChandra(player1, 5);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("−2 rejects a land as a target")
    void minusTwoRejectsLandTarget() {
        addReadyChandra(player1, 5);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoKillsCreatureWithTwoToughness() {
        addReadyChandra(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void minusTwoRemovesLoyaltyFromPlaneswalker() {
        addReadyChandra(player1, 5);
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        opposingChandra.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 2, null, opposingChandra.getId());
        harness.passBothPriorities();

        assertThat(opposingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusTwoResolvesAfterSpendingLastLoyalty() {
        Permanent chandra = addReadyChandra(player1, 2);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(chandra);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void cannotActivateAnotherLoyaltyAbilityInSameTurn() {
        addReadyChandra(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSpendMoreLoyaltyThanAvailable() {
        Permanent chandra = addReadyChandra(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void plusOneIncludesElementalsPresentWhenAbilityResolves() {
        addReadyChandra(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    void manaProducingLoyaltyAbilityCannotBeActivatedDuringCombat() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}

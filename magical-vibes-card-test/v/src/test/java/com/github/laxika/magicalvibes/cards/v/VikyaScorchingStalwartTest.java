package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VikyaScorchingStalwart.class, Forest.class, JaceBeleren.class, SerraAngel.class, AirElemental.class, FyndhornElves.class})
class VikyaScorchingStalwartTest extends BaseCardTest {

    @Test
    void trainingPutsACounterOnVikya() {
        Permanent vikya = addReadyVikya();
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(vikya),
                gd.playerBattlefields.get(player1.getId()).indexOf(airElemental)));
        harness.passBothPriorities();

        assertThat(vikya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untapSymbolAndDiscardCostDealPowerDamageToAnyTarget() {
        Permanent vikya = addReadyVikya();
        vikya.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(vikya.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void excessDamageToCreatureDrawsACard() {
        Permanent vikya = addReadyVikya();
        Permanent target = addCreatureReady(player2, new FyndhornElves());
        vikya.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Elves");
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(vikya.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent vikya = addReadyVikya();
        vikya.tap();
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void excessDamageToNoncreaturePlaneswalkerDoesNotDraw() {
        Permanent vikya = addReadyVikya();
        vikya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        vikya.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void exactlyLethalDamageToCreatureDoesNotDraw() {
        Permanent vikya = addReadyVikya();
        vikya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        vikya.tap();
        Permanent target = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void damageToPlayerDoesNotDraw() {
        Permanent vikya = addReadyVikya();
        vikya.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPayUntapCostWhileUntapped() {
        addReadyVikya();
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void summoningSickCreatureCannotPayUntapCost() {
        Permanent vikya = addReadyVikya();
        vikya.setSummoningSick(true);
        vikya.tap();
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vikya.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void trainingDoesNotTriggerWhenAttackingAlone() {
        Permanent vikya = addReadyVikya();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(vikya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void trainingDoesNotTriggerWithEqualPowerAttacker() {
        Permanent vikya = addReadyVikya();
        vikya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(vikya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void damageUsesPowerAtResolution() {
        Permanent vikya = addReadyVikya();
        vikya.tap();
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        vikya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void excessDamageAccountsForPreviouslyMarkedDamage() {
        Permanent vikya = addReadyVikya();
        vikya.tap();
        Permanent target = addCreatureReady(player2, new SerraAngel());
        target.setMarkedDamage(3);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    private Permanent addReadyVikya() {
        return addCreatureReady(player1, new VikyaScorchingStalwart());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VikyaScorchingStalwart.class, Forest.class, GoliathBeetle.class, SerraAngel.class, AirElemental.class, FyndhornElves.class})
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
        addReadyVikya();
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVikya() {
        return addCreatureReady(player1, new VikyaScorchingStalwart());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

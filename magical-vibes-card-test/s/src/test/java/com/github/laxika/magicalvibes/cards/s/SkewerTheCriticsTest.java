package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DomriChaosBringer;
import com.github.laxika.magicalvibes.cards.g.GravelHideGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkewerTheCritics.class, GravelHideGoblin.class, DomriChaosBringer.class})
class SkewerTheCriticsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player for its normal cost")
    void dealsThreeDamageForNormalCost() {
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage when cast for spectacle")
    void dealsThreeDamageForSpectacleCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void killsTargetCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new GravelHideGoblin());
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Gravel-Hide Goblin");
        harness.assertInGraveyard(player2, "Gravel-Hide Goblin");
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsThreeDamageToPlaneswalkerWithoutEnablingSpectacle() {
        var target = harness.addToBattlefieldAndReturn(player2, new DomriChaosBringer());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SkewerTheCritics(), new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageFromFirstSpellEnablesSpectacleForSecondSpell() {
        harness.setHand(player1, List.of(new SkewerTheCritics(), new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void losingLifeYourselfDoesNotEnableSpectacle() {
        harness.setHand(player1, List.of(new SkewerTheCritics(), new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spectacleRemainsAvailableAfterOpponentRegainsLife() {
        harness.setHand(player1, List.of(new SkewerTheCritics(), new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.setLife(player2, 23);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}

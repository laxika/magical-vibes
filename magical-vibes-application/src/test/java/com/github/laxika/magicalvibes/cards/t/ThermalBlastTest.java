package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.r.RabidElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThermalBlast.class, RabidElephant.class, DuskImp.class})
class ThermalBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage with fewer than seven cards in the graveyard")
    void dealsThreeDamageWithoutThreshold() {
        var target = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()));

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals 5 damage with seven cards in the graveyard")
    void dealsFiveDamageWithThreshold() {
        var target = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Rabid Elephant");
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        var target = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new RabidElephant());
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Checks threshold when the spell resolves")
    void thresholdIsCheckedWhenTheSpellResolves() {
        var target = harness.addToBattlefieldAndReturn(player2, new RabidElephant());
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ThermalBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

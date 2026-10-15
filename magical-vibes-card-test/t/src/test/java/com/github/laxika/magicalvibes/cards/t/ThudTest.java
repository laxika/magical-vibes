package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BristlingBoar;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.v.VivienReid;
import com.github.laxika.magicalvibes.cards.w.WallOfMist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thud.class, GreenwoodSentinel.class, BristlingBoar.class, WallOfMist.class, VivienReid.class})
class ThudTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the sacrificed creature's power to a target player")
    void dealsSacrificedPowerToPlayer() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Deals sacrificed power to a target creature")
    void dealsSacrificedPowerToCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective power")
    void usesEffectivePower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCard();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndOnlyChosenCreatureIsSacrificed() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        harness.addToBattlefield(player1, new WallOfMist());
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Bristling Boar");
        harness.assertNotOnBattlefield(player1, "Bristling Boar");
        harness.assertOnBattlefield(player1, "Wall of Mist");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void zeroPowerCreaturePaysCostButDealsNoDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WallOfMist());
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Wall of Mist");
        harness.assertInGraveyard(player1, "Thud");
    }

    @Test
    void negativePowerCreatureDealsNoDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WallOfMist());
        sacrifice.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Wall of Mist");
    }

    @Test
    void dealsDamageToPlaneswalker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VivienReid());
        target.setCounterCount(CounterType.LOYALTY, 5);
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Vivien Reid");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new BristlingBoar());
        prepareCard();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Bristling Boar");
        harness.assertInHand(player1, "Thud");
    }

    @Test
    void canTargetController() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        prepareCard();

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new Thud()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

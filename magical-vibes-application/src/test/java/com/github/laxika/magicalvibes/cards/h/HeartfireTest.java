package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Heartfire.class, PouncingLynx.class, SarkhanTheMasterless.class})
class HeartfireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and deals 4 damage to a player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Heartfire");
    }

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsDamageToTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.addToBattlefield(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(
                player1, 0, harness.getPermanentId(player2, "Pouncing Lynx"), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pouncing Lynx");
    }

    @Test
    @DisplayName("Cannot cast without a creature or planeswalker to sacrifice")
    void cannotCastWithoutCreatureOrPlaneswalkerToSacrifice() {
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Can sacrifice a planeswalker as the additional cost")
    void sacrificesPlaneswalkerAndDealsDamageToPlayer() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sacrifice.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Sarkhan the Masterless");
        harness.assertInGraveyard(player1, "Sarkhan the Masterless");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Heartfire");
    }

    @Test
    @DisplayName("Deals 4 damage to a target planeswalker")
    void dealsDamageToTargetPlaneswalker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sarkhan the Masterless");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target the sacrificed creature but does not resolve after it leaves")
    void canTargetCreatureSacrificedAsCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Pouncing Lynx");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heartfire");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the additional cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new PouncingLynx());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new Heartfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, player2.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Pouncing Lynx");
        harness.assertInHand(player1, "Heartfire");
        harness.assertLife(player2, 20);
    }
}

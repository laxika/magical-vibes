package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.c.ChandraAblaze;
import com.github.laxika.magicalvibes.cards.g.GrazingGladehart;
import com.github.laxika.magicalvibes.cards.k.KabiraCrossroads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PunishingFire.class, HealingSalve.class, GrazingGladehart.class, KabiraCrossroads.class,
        ChandraAblaze.class})
class PunishingFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player")
    void dealsTwoDamageToPlayer() {
        harness.setHand(player1, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent life gain lets you pay {R} to return it from the graveyard")
    void opponentLifeGainReturnsItToHandWhenPaid() {
        PunishingFire punishingFire = new PunishingFire();
        harness.setGraveyard(player1, List.of(punishingFire));
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Punishing Fire");
    }

    @Test
    @DisplayName("Its own controller gaining life does not trigger it")
    void ownLifeGainDoesNotTrigger() {
        PunishingFire punishingFire = new PunishingFire();
        harness.setGraveyard(player1, List.of(punishingFire));
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Punishing Fire");
    }

    @Test
    void dealsLethalDamageToCreature() {
        harness.addToBattlefield(player2, new GrazingGladehart());
        harness.setHand(player1, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grazing Gladehart"));

        harness.assertNotOnBattlefield(player2, "Grazing Gladehart");
        harness.assertInGraveyard(player2, "Grazing Gladehart");
        harness.assertInGraveyard(player1, "Punishing Fire");
    }

    @Test
    void damagesPlaneswalkerInsteadOfItsController() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeGainDoesNotTriggerWhileCardIsInHand() {
        harness.setHand(player1, List.of(new PunishingFire()));
        harness.setHand(player2, List.of(new KabiraCrossroads()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "Punishing Fire");
    }

    @Test
    void decliningPaymentLeavesCardInGraveyard() {
        harness.setGraveyard(player1, List.of(new PunishingFire()));
        harness.setHand(player2, List.of(new KabiraCrossroads()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Punishing Fire");
        harness.assertNotInHand(player1, "Punishing Fire");
    }

    @Test
    void colorlessManaCannotPayForReturn() {
        harness.setGraveyard(player1, List.of(new PunishingFire()));
        harness.setHand(player2, List.of(new KabiraCrossroads()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Punishing Fire");
        harness.assertNotInHand(player1, "Punishing Fire");
    }

    @Test
    void eachCopyRequiresItsOwnPayment() {
        PunishingFire first = new PunishingFire();
        PunishingFire second = new PunishingFire();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player2, List.of(new KabiraCrossroads()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(first, second);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}

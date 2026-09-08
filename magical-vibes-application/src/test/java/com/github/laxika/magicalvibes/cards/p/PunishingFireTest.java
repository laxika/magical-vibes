package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PunishingFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player")
    void dealsTwoDamageToPlayer() {
        harness.setHand(player1, List.of(new PunishingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player2, 0, 0, player2.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Punishing Fire");
    }
}

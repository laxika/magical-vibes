package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LimDLsHex.class, CircleOfProtectionBlack.class})
class LimDLsHexTest extends BaseCardTest {

    @Test
    @DisplayName("Both players declining take 1 damage each")
    void bothDeclineTakeDamage() {
        harness.addToBattlefield(player1, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → first may-pay

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, life1 - 1);
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("Paying {B} avoids damage for that player only")
    void payingBlackAvoidsOwnDamage() {
        harness.addToBattlefield(player1, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, life1);
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("Paying {3} avoids damage")
    void payingThreeAvoidsDamage() {
        harness.addToBattlefield(player1, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, life1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still deals damage, then opponent is prompted")
    void acceptWithoutManaDealsDamage() {
        harness.addToBattlefield(player1, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // accept with empty pool → damage

        harness.assertLife(player1, life1 - 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, life1);
        harness.assertLife(player2, life2);
    }

    @Test
    @DisplayName("Triggers during the controller's upkeep when controlled by player two")
    void triggersDuringControllerUpkeepForPlayerTwo() {
        harness.addToBattlefield(player2, new LimDLsHex());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, life1 - 1);
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("Unpaid damage can be prevented for one player without protecting the other")
    void damageCanBePreventedForOnePlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent hex = harness.addToBattlefieldAndReturn(player1, new LimDLsHex());
        harness.addToBattlefield(player1, new CircleOfProtectionBlack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hex.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}

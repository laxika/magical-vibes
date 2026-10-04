package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfAncientWars.class, HondenOfSeeingWinds.class, TezzeretBetrayerOfFlesh.class})
class GoShintaiOfAncientWarsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} deals damage equal to the number of Shrines to a target player")
    void paysToDealDamageForEachShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the payment deals no damage")
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability triggers only during its controller's end step")
    void triggersOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Payment creates a separate damage trigger that can be responded to")
    void damageWaitsForReflexiveTriggerToResolve() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handlePermanentChosen(player1, player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage can target a planeswalker and removes loyalty instead of player life")
    void damagesPlaneswalker() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opposing Shrines do not increase damage, and the controller is a legal target")
    void countsOnlyControlledShrinesAndCanTargetController() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.addToBattlefield(player2, new GoShintaiOfAncientWars());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Shrines are counted when the reflexive damage trigger resolves")
    void countsShrinesAtDamageResolution() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handlePermanentChosen(player1, player2.getId()));
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Accepting without enough mana creates no damage trigger")
    void cannotDealDamageWithoutPaying() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

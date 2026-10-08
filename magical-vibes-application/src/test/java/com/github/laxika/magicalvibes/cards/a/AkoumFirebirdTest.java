package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.s.Stonefury;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkoumFirebird.class, Mountain.class, EvolvingWilds.class, Stonefury.class})
class AkoumFirebirdTest extends BaseCardTest {

    @Test
    @DisplayName("Must attack each combat if able")
    void mustAttackEachCombatIfAble() {
        addCreatureReady(player1, new AkoumFirebird());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("May pay {4}{R}{R} to return Akoum Firebird from the graveyard")
    void payingManaReturnsFirebird() {
        AkoumFirebird firebird = new AkoumFirebird();
        harness.setGraveyard(player1, List.of(firebird));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Akoum Firebird");
        harness.assertOnBattlefield(player1, "Akoum Firebird");
    }

    @Test
    @DisplayName("Declining the payment keeps Akoum Firebird in the graveyard")
    void decliningPaymentKeepsFirebirdInGraveyard() {
        AkoumFirebird firebird = new AkoumFirebird();
        harness.setGraveyard(player1, List.of(firebird));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Akoum Firebird");
        harness.assertNotOnBattlefield(player1, "Akoum Firebird");
    }

    @Test
    @DisplayName("Cannot return Akoum Firebird without enough mana")
    void insufficientManaKeepsFirebirdInGraveyard() {
        AkoumFirebird firebird = new AkoumFirebird();
        harness.setGraveyard(player1, List.of(firebird));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Akoum Firebird");
        harness.assertNotOnBattlefield(player1, "Akoum Firebird");
    }

    @Test
    @DisplayName("An opponent's land does not trigger Akoum Firebird's graveyard ability")
    void opponentLandDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new AkoumFirebird()));
        prepareMain(player2);
        harness.setHand(player2, List.of(new Mountain()));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedFirebirdIsNotRequiredToAttack() {
        addCreatureReady(player1, new AkoumFirebird()).setTapped(true);

        declareAttackers(List.of());
    }

    @Test
    void newlyReturnedFirebirdMustAttackImmediately() {
        harness.setGraveyard(player1, List.of(new AkoumFirebird()));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void paymentRequiresTwoRedMana() {
        harness.setGraveyard(player1, List.of(new AkoumFirebird()));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Akoum Firebird");
        harness.assertNotOnBattlefield(player1, "Akoum Firebird");
    }

    @Test
    void eachFirebirdRequiresItsOwnPayment() {
        AkoumFirebird first = new AkoumFirebird();
        AkoumFirebird second = new AkoumFirebird();
        harness.setGraveyard(player1, List.of(first, second));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Akoum Firebird")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof AkoumFirebird).hasSize(1);
    }

    @Test
    void olderLandfallTriggerCannotReturnFirebirdAfterItDiesAgain() {
        AkoumFirebird firebird = new AkoumFirebird();
        harness.setGraveyard(player1, List.of(firebird));
        prepareMain(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EvolvingWilds(), new Stonefury()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        var returned = findPermanent(player1, "Akoum Firebird");
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Akoum Firebird");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Akoum Firebird");
        harness.assertNotOnBattlefield(player1, "Akoum Firebird");
    }

    private void prepareMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

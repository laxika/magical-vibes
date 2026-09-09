package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({AkoumFirebird.class, Mountain.class})
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(firebird.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firebird.getId()));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firebird.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firebird.getId()));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firebird.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firebird.getId()));
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

    private void prepareMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

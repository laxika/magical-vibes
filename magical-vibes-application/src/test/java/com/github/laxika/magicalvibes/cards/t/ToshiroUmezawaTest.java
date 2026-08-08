package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToshiroUmezawaTest extends BaseCardTest {

    /** Player1 edicts away player2's only creature, firing Toshiro's trigger. */
    private void killOpponentCreature() {
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent's creature dying lets the targeted instant be cast from the graveyard, and it is exiled instead of returning there")
    void castsInstantFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));

        killOpponentCreature();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve the trigger — permission granted

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Only instant cards in the controller's own graveyard are legal targets")
    void onlyOwnInstantsAreTargetable() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card ownInstant = new Shock();
        Card ownSorcery = new LavaSpike();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownInstant, ownSorcery)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentInstant)));

        killOpponentCreature();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
    }

    @Test
    @DisplayName("Trigger is skipped when there is no instant card to target")
    void triggerSkippedWithoutLegalTarget() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new LavaSpike())));

        killOpponentCreature();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own creature dying does not trigger the ability")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("The granted permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));

        killOpponentCreature();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).containsKey(shock.getId());

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TersaLightshatter.class, Forest.class, GrizzlyBears.class, Shock.class})
class TersaLightshatterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by discarding up to two cards and drawing that many")
    void entersWithCappedRummage() {
        Card discardOne = new GrizzlyBears();
        Card discardTwo = new GrizzlyBears();
        Card kept = new GrizzlyBears();
        Card drawOne = new Forest();
        Card drawTwo = new Forest();
        harness.setLibrary(player1, List.of(drawOne, drawTwo));
        harness.setHand(player1, List.of(new TersaLightshatter(), discardOne, discardTwo, kept));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(kept, drawOne, drawTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardOne, discardTwo);
    }

    @Test
    @DisplayName("Does not trigger while the controller has fewer than seven graveyard cards")
    void attackDoesNotTriggerBelowGraveyardThreshold() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack exiles a random graveyard card and grants permission to play it this turn")
    void attackExilesRandomGraveyardCardForThisTurn() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(exiled.getId());

        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        gs.playCardFromExile(gd, player1, exiled.getId(), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the graveyard threshold when the attack trigger resolves")
    void attackTriggerDoesNothingIfThresholdIsLostBeforeResolution() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}

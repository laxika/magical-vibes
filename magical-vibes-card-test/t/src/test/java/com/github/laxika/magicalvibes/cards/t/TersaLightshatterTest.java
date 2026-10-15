package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        resolveAllTriggers();

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

    @Test
    @DisplayName("Choosing zero discards keeps the hand and library unchanged")
    void mayDiscardZero() {
        Card kept = new Forest();
        Card top = new Forest();
        harness.setHand(player1, List.of(new TersaLightshatter(), kept));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A single remaining hand card can be discarded to draw one card")
    void discardsOneAndDrawsOne() {
        Card discarded = new Forest();
        Card drawn = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of(new TersaLightshatter(), discarded));
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Entering with an empty hand neither draws nor asks for a discard")
    void emptyHandDoesNotDraw() {
        Card top = new Forest();
        harness.setHand(player1, List.of(new TersaLightshatter()));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Tersa to attack on the turn it enters")
    void canAttackImmediatelyAfterBeingCast() {
        harness.setHand(player1, List.of(new TersaLightshatter()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cards in the opponent's graveyard do not satisfy the attack threshold")
    void opponentsGraveyardDoesNotCount() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("An exiled land can be played in the main phase but not during combat")
    void mayPlayExiledLandWithNormalTiming() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, exiled.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(exiled));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Unused exile permission expires at the end of the turn while the card stays exiled")
    void unusedPermissionExpires() {
        addCreatureReady(player1, new TersaLightshatter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

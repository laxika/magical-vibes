package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LichsMastery.class, GrizzlyBears.class, Shock.class, PlatinumAngel.class, Naturalize.class})
class LichsMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Controller doesn't lose at 0 life with Lich's Mastery")
    void controllerDoesNotLoseAtZeroLife() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 2);

        // Shock player1 to bring them to 0
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Gaining life draws that many cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Directly call the service to gain life
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.passBothPriorities();

        // The triggered ability draws only when it resolves.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Losing life waits for resolution and lets the controller choose graveyard cards")
    void lifeLossAllowsChoosingGraveyardCards() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Put 3 cards in graveyard
        Card gy1 = new GrizzlyBears();
        Card gy2 = new GrizzlyBears();
        Card gy3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(gy1, gy2, gy3));

        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        // Shock player1 for 2 damage
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gy1, gy2, gy3);
    }

    @Test
    @DisplayName("Losing life exiles from hand when graveyard is empty")
    void lifeLossExilesFromHandWhenGraveyardEmpty() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Clear graveyard
        harness.setGraveyard(player1, List.of());

        // Put 3 cards in hand
        Card h1 = new GrizzlyBears();
        Card h2 = new GrizzlyBears();
        Card h3 = new GrizzlyBears();
        harness.setHand(player1, List.of(h1, h2, h3));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Shock player1 for 2 damage
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(h1, h2, h3);
    }

    @Test
    @DisplayName("Losing life exiles permanents when graveyard and hand are empty")
    void lifeLossExilesPermanentsWhenOtherZonesEmpty() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Clear graveyard and hand
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());

        // Multiple permanents leave a choice of which two to exile.
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Resolve the damage before resolving the exile trigger.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("Controller loses the game when Lich's Mastery is destroyed")
    void controllerLosesWhenDestroyed() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        Permanent lichsMastery = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Lich's Mastery"));

        // Directly remove Lich's Mastery from the battlefield (simulating destruction)
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lichsMastery));

        // The LTB trigger puts TargetPlayerLosesGameEffect on the stack
        // Pass priorities to resolve it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Controller loses the game when Lich's Mastery is exiled")
    void controllerLosesWhenExiled() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        Permanent lichsMastery = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Lich's Mastery"));

        // Exile Lich's Mastery
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(gd, lichsMastery));

        // Pass priorities to resolve the lose-game trigger
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Controller loses the game when Lich's Mastery is bounced")
    void controllerLosesWhenBounced() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        Permanent lichsMastery = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Lich's Mastery"));

        // Bounce Lich's Mastery
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, lichsMastery));

        // Pass priorities to resolve the lose-game trigger
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Lich's Mastery LTB trigger respects Platinum Angel can't-lose")
    void ltbTriggerRespectsCannotLose() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 20);

        // Remove Lich's Mastery — the LTB trigger fires but Platinum Angel prevents the loss
        Permanent lichsMastery = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Lich's Mastery"));
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lichsMastery));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Platinum Angel prevents the loss
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Opponent's Lich's Mastery does not protect controller from losing")
    void opponentsLichsMasteryDoesNotProtectUs() {
        harness.addToBattlefield(player2, new LichsMastery());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Drawing from an empty library does not defeat the controller")
    void emptyLibraryDoesNotDefeatController() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An opponent gaining life does not draw cards for either player")
    void opponentsLifeGainDoesNotDrawCards() {
        harness.addToBattlefield(player1, new LichsMastery());
        int ourHand = gd.playerHands.get(player1.getId()).size();
        int theirHand = gd.playerHands.get(player2.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ourHand);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(theirHand);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the enchantment")
    void opponentCannotTargetMastery() {
        Permanent mastery = harness.addToBattlefieldAndReturn(player1, new LichsMastery());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, mastery.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Lich's Mastery");
    }
}

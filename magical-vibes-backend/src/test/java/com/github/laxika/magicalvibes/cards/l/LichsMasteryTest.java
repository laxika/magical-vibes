package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LichsMasteryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Lich's Mastery has correct effects")
    void hasCorrectEffects() {
        LichsMastery card = new LichsMastery();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof GrantKeywordEffect gke && gke.keyword() == Keyword.HEXPROOF)
                .anyMatch(e -> e instanceof CantLoseGameEffect);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE).getFirst())
                .isInstanceOf(DrawCardsEqualToLifeGainedEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_LOSES_LIFE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_LOSES_LIFE).getFirst())
                .isInstanceOf(ExileForEachLifeLostEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD).getFirst())
                .isInstanceOf(ControllerLosesGameOnLeavesEffect.class);
    }

    // ===== Can't lose the game =====

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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    // ===== Life gain draws cards =====

    @Test
    @DisplayName("Gaining life draws that many cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Directly call the service to gain life
        var lifeService = harness.getLifeResolutionService();
        lifeService.applyGainLife(gd, player1.getId(), 3);

        // Should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore - 3);
    }

    // ===== Life loss exiles cards =====

    @Test
    @DisplayName("Losing life exiles cards from graveyard first")
    void lifeLossExilesFromGraveyardFirst() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Put 3 cards in graveyard
        Card gy1 = new GrizzlyBears();
        Card gy2 = new GrizzlyBears();
        Card gy3 = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(gy1, gy2, gy3));

        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        // Shock player1 for 2 damage
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage → 2 life lost → 2 cards exiled from graveyard
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardSizeBefore - 2);

        // Exiled cards should be in exile zone
        List<Card> exiled = gd.playerExiledCards.get(player1.getId());
        assertThat(exiled).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("Losing life exiles from hand when graveyard is empty")
    void lifeLossExilesFromHandWhenGraveyardEmpty() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Clear graveyard
        gd.playerGraveyards.get(player1.getId()).clear();

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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage → 2 life lost → 2 cards exiled from hand
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 2);
    }

    @Test
    @DisplayName("Losing life exiles permanents when graveyard and hand are empty")
    void lifeLossExilesPermanentsWhenOtherZonesEmpty() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        // Clear graveyard and hand
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.setHand(player1, List.of());

        // Add a creature to battlefield
        harness.addToBattlefield(player1, new GrizzlyBears());

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Shock player1 for 2 damage — should exile the creature and then Lich's Mastery
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // At least the creature should have been exiled
        assertThat(gd.playerBattlefields.get(player1.getId()).size()).isLessThan(battlefieldSizeBefore);
    }

    // ===== Leaves the battlefield — lose the game =====

    @Test
    @DisplayName("Controller loses the game when Lich's Mastery is destroyed")
    void controllerLosesWhenDestroyed() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.setLife(player1, 20);

        Permanent lichsMastery = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof LichsMastery)
                .findFirst().orElseThrow();

        // Directly remove Lich's Mastery from the battlefield (simulating destruction)
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lichsMastery);

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

        Permanent lichsMastery = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof LichsMastery)
                .findFirst().orElseThrow();

        // Exile Lich's Mastery
        harness.getPermanentRemovalService().removePermanentToExile(gd, lichsMastery);

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

        Permanent lichsMastery = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof LichsMastery)
                .findFirst().orElseThrow();

        // Bounce Lich's Mastery
        harness.getPermanentRemovalService().removePermanentToHand(gd, lichsMastery);

        // Pass priorities to resolve the lose-game trigger
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Lich's Mastery LTB respects "can't lose" =====

    @Test
    @DisplayName("Lich's Mastery LTB trigger respects Platinum Angel can't-lose")
    void ltbTriggerRespectsCannotLose() {
        harness.addToBattlefield(player1, new LichsMastery());
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 20);

        // Remove Lich's Mastery — the LTB trigger fires but Platinum Angel prevents the loss
        Permanent lichsMastery = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof LichsMastery)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lichsMastery);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Platinum Angel prevents the loss
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    // ===== Opponent's Lich's Mastery doesn't affect us =====

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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

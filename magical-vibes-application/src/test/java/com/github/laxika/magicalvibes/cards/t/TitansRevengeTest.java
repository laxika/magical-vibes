package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitansRevenge.class, MothdustChangeling.class, Mutavault.class})
class TitansRevengeTest extends BaseCardTest {

    private void castAtTarget(int x, UUID targetId) {
        harness.setHand(player1, List.of(new TitansRevenge()));
        harness.addMana(player1, ManaColor.RED, 2 + x);

        harness.castAndResolveSorcery(player1, 0, x, targetId);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void castAtPlayer2(int x) {
        harness.setLife(player2, 20);
        castAtTarget(x, player2.getId());
    }

    @Test
    @DisplayName("Deals X damage to any target")
    void dealsXDamageToTarget() {
        // Equal mana values on top → clash is a loss, isolating the damage effect.
        harness.setLibrary(player1, List.of(new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castAtPlayer2(4);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals X damage to a creature target")
    void dealsXDamageToCreatureTarget() {
        MothdustChangeling target = new MothdustChangeling();
        harness.addToBattlefield(player2, target);
        harness.setLibrary(player1, List.of(new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castAtTarget(1, harness.getPermanentId(player2, "Mothdust Changeling"));

        harness.assertNotOnBattlefield(player2, "Mothdust Changeling");
    }

    @Test
    @DisplayName("X can be zero while a won clash returns Titan's Revenge")
    void zeroDamageStillReturnsSpellAfterWinningClash() {
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castAtPlayer2(0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInHand(player1, "Titan's Revenge");
    }

    @Test
    @DisplayName("Winning the clash returns Titan's Revenge to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Higher mana value on top for player1 (Mothdust Changeling MV 1 > Mutavault MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castAtPlayer2(3);

        harness.assertInHand(player1, "Titan's Revenge");
        harness.assertNotInGraveyard(player1, "Titan's Revenge");
    }

    @Test
    @DisplayName("Losing the clash sends Titan's Revenge to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Lower mana value on top for player1 (Mutavault MV 0 < Mothdust Changeling MV 1) → player1 loses.
        harness.setLibrary(player1, List.of(new Mutavault()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castAtPlayer2(3);

        harness.assertInGraveyard(player1, "Titan's Revenge");
        harness.assertNotInHand(player1, "Titan's Revenge");
    }

    @Test
    @DisplayName("A tied clash does not return the spell")
    void tiedClashSendsSpellToGraveyard() {
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castAtPlayer2(2);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Titan's Revenge");
        harness.assertNotInHand(player1, "Titan's Revenge");
    }

    @Test
    @DisplayName("Both players may bottom their revealed cards without changing the clash winner")
    void bottomingRevealedCardsPreservesClashWinner() {
        MothdustChangeling winningCard = new MothdustChangeling();
        Mutavault nextCard = new Mutavault();
        Mutavault losingCard = new Mutavault();
        MothdustChangeling opponentNextCard = new MothdustChangeling();
        harness.setLibrary(player1, List.of(winningCard, nextCard));
        harness.setLibrary(player2, List.of(losingCard, opponentNextCard));
        harness.setHand(player1, List.of(new TitansRevenge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
        harness.assertNotInHand(player1, "Titan's Revenge");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, winningCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNextCard, losingCard);
        harness.assertInHand(player1, "Titan's Revenge");
        harness.assertNotInGraveyard(player1, "Titan's Revenge");
    }

    @Test
    @DisplayName("An illegal sole target prevents damage and the clash")
    void illegalTargetPreventsClash() {
        MothdustChangeling target = new MothdustChangeling();
        MothdustChangeling topCard = new MothdustChangeling();
        Mutavault opponentTopCard = new Mutavault();
        harness.addToBattlefield(player2, target);
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new TitansRevenge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Mothdust Changeling"));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        harness.assertInGraveyard(player1, "Titan's Revenge");
        harness.assertNotInHand(player1, "Titan's Revenge");
    }
}

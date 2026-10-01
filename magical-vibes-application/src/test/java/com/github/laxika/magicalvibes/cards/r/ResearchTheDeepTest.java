package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ResearchTheDeep.class, MothdustChangeling.class, Mutavault.class})
class ResearchTheDeepTest extends BaseCardTest {

    private void castResearch() {
        harness.setHand(player1, List.of(new ResearchTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws the top card before a tied clash and puts the spell in the graveyard")
    void drawsACard() {
        // Draw happens first (Mutavault), then the clash reveals the next card (Mutavault, MV 0)
        // against the opponent's Mutavault (MV 0), so no one wins.
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player2.getId()).addFirst(new Mutavault());

        castResearch();

        harness.assertInHand(player1, "Mutavault");
        harness.assertInGraveyard(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Winning the clash returns Research the Deep to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Draw Mutavault, then clash with Mothdust Changeling (MV 1) vs opponent's Mutavault
        // (MV 0), so player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new MothdustChangeling());
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player2.getId()).addFirst(new Mutavault());

        castResearch();

        harness.assertInHand(player1, "Research the Deep");
        harness.assertNotInGraveyard(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Losing the clash sends Research the Deep to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Draw Mutavault, then clash with Mutavault (MV 0) vs opponent's Mothdust Changeling
        // (MV 1), so player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player2.getId()).addFirst(new MothdustChangeling());

        castResearch();

        harness.assertInGraveyard(player1, "Research the Deep");
        harness.assertNotInHand(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Offers a choice of putting the revealed cards on top or bottom")
    void offersLibraryPlacementChoicesAfterClash() {
        gd.playerDecks.get(player1.getId()).addFirst(new MothdustChangeling());
        gd.playerDecks.get(player1.getId()).addFirst(new Mutavault());
        gd.playerDecks.get(player2.getId()).addFirst(new Mutavault());

        castResearch();

        org.assertj.core.api.Assertions.assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}

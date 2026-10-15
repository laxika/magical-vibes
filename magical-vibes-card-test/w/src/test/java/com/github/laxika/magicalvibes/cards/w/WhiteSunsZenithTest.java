package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FuelForTheCause;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteSunsZenith.class, FuelForTheCause.class})
class WhiteSunsZenithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 6); // X=3: {3}{W}{W}{W} = 6

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(WhiteSunsZenith.class);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=3 creates 3 Cat tokens")
    void xEqualsThreeCreatesThreeCatTokens() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 6); // X=3: {3}{W}{W}{W} = 6

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        List<Permanent> catTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .toList();
        assertThat(catTokens).hasSize(3);
    }

    @Test
    @DisplayName("Cat tokens are 2/2 white Cats")
    void catTokensHaveCorrectProperties() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1: {1}{W}{W}{W} = 4

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent catToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .findFirst().orElseThrow();

        assertThat(catToken.getCard().getPower()).isEqualTo(2);
        assertThat(catToken.getCard().getToughness()).isEqualTo(2);
        assertThat(catToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(catToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(catToken.getCard().getSubtypes()).contains(CardSubtype.CAT);
    }

    @Test
    @DisplayName("X=0 creates no tokens")
    void xZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 3); // X=0: {0}{W}{W}{W} = 3

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        long catTokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .count();
        assertThat(catTokenCount).isZero();
    }

    @Test
    @DisplayName("White Sun's Zenith is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1: {1}{W}{W}{W} = 4

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        // Not in graveyard
        harness.assertNotInGraveyard(player1, "White Sun's Zenith");
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("White Sun's Zenith"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new WhiteSunsZenith()));
        harness.addMana(player1, ManaColor.WHITE, 4); // X=1

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=0 still shuffles the physical spell into its owner's library")
    void xZeroStillShufflesIntoLibrary() {
        WhiteSunsZenith zenith = new WhiteSunsZenith();
        harness.setHand(player1, List.of(zenith));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize + 1).contains(zenith);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "White Sun's Zenith");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player's Zenith creates tokens for that player and returns to their library")
    void secondPlayerReceivesTokensAndShuffledSpell() {
        WhiteSunsZenith zenith = new WhiteSunsZenith();
        harness.setHand(player2, List.of(zenith));
        harness.addMana(player2, ManaColor.WHITE, 5);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player2, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.isAttacking()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 1).contains(zenith);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(zenith);
        harness.assertNotInGraveyard(player2, "White Sun's Zenith");
    }

    @Test
    @DisplayName("Countering Zenith prevents token creation and sends it to the graveyard")
    void counteredZenithDoesNotCreateTokensOrShuffleIntoLibrary() {
        WhiteSunsZenith zenith = new WhiteSunsZenith();
        harness.setHand(player1, List.of(zenith));
        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player2, ManaColor.BLUE, 4);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, 3, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, zenith.getId());

        harness.assertInGraveyard(player1, "White Sun's Zenith");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize).doesNotContain(zenith);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}

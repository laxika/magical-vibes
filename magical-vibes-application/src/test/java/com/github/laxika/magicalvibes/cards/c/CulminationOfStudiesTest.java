package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArchwayCommons;
import com.github.laxika.magicalvibes.cards.b.BurrogBefuddler;
import com.github.laxika.magicalvibes.cards.e.ElementalMasterpiece;
import com.github.laxika.magicalvibes.cards.i.IgneousInspiration;
import com.github.laxika.magicalvibes.cards.p.ProfessorOfZoomancy;
import com.github.laxika.magicalvibes.cards.t.TorbranThaneOfRedFell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CulminationOfStudies.class, ArchwayCommons.class, BurrogBefuddler.class,
        ElementalMasterpiece.class, IgneousInspiration.class, ProfessorOfZoomancy.class,
        TorbranThaneOfRedFell.class})
class CulminationOfStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X cards and applies land, blue, and red riders independently")
    void exilesCardsAndAppliesAllRiders() {
        Card land = new ArchwayCommons();
        Card blue = new BurrogBefuddler();
        Card red = new IgneousInspiration();
        Card blueAndRed = new ElementalMasterpiece();
        Card firstDraw = new ProfessorOfZoomancy();
        Card secondDraw = new ArchwayCommons();

        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(land, blue, red, blueAndRed, firstDraw, secondDraw));
        addManaForX(player1, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(land.getId(), blue.getId(), red.getId(), blueAndRed.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                        p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiles only the cards available when X exceeds the library size")
    void exilesOnlyAvailableCards() {
        Card land = new ArchwayCommons();
        Card blue = new BurrogBefuddler();

        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(land, blue));
        addManaForX(player1, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 5);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(land.getId(), blue.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                        p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage from multiple red cards is a single event for Torbran")
    void multipleRedCardsDealOneDamageEvent() {
        harness.addToBattlefield(player1, new TorbranThaneOfRedFell());
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(new IgneousInspiration(), new IgneousInspiration()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addManaForX(player1, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.exiledCards).hasSize(2);
    }

    @Test
    @DisplayName("X zero leaves the library intact and applies no riders")
    void zeroExilesNothing() {
        Card land = new ArchwayCommons();
        Card multicolor = new ElementalMasterpiece();
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(land, multicolor));
        harness.setLife(player2, 20);
        addManaForX(player1, 0);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, multicolor);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An empty library exiles nothing and does not cause a draw attempt")
    void emptyLibraryAppliesNoRiders() {
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        addManaForX(player1, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Nonland cards of other colors do not produce any riders")
    void otherColorsProduceNoRiders() {
        Card green = new ProfessorOfZoomancy();
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(green));
        harness.setLife(player2, 20);
        addManaForX(player1, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(green.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal damage still occurs after an empty-library draw, causing a draw")
    void emptyLibraryDrawAndLethalDamageCauseDraw() {
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(new ElementalMasterpiece()));
        harness.setLife(player2, 1);
        addManaForX(player1, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Each exiled land produces an untapped Treasure")
    void multipleLandsCreateUntappedTreasures() {
        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(new ArchwayCommons(), new ArchwayCommons()));
        addManaForX(player1, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addManaForX(Player player, int x) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, x);
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulRegrowth.class, Forest.class, Mountain.class})
class VengefulRegrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three lands tapped and creates that many Plant Warriors")
    void returnsLandsAndCreatesMatchingTokens() {
        VengefulRegrowth spell = new VengefulRegrowth();
        Card first = new Forest();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.castFromHand(player1, spell, "{4}{G}{G}");
        choose(first, second, third);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId(), third.getId());
        assertThat(battlefield).filteredOn(permanent -> !permanent.getCard().isToken())
                .allMatch(Permanent::isTapped);
        List<Permanent> tokens = battlefield.stream().filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PLANT, CardSubtype.WARRIOR);
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, token, Keyword.REACH)).isTrue();
        });
    }

    @Test
    @DisplayName("Flashback returns the selected lands, creates matching tokens, and exiles the spell")
    void flashbackReturnsLandsAndExilesSpell() {
        VengefulRegrowth spell = new VengefulRegrowth();
        Card first = new Forest();
        Card second = new Mountain();
        harness.setGraveyard(player1, List.of(spell, first, second));
        addMana(6, 2);

        harness.castFlashback(player1, 0);
        choose(first, second);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Can resolve with no lands in either graveyard")
    void resolvesWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new VengefulRegrowth(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vengeful Regrowth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May select zero lands even when lands are available")
    void mayChooseNoLands() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.castFromHand(player1, new VengefulRegrowth(), "{4}{G}{G}");
        choose();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Vengeful Regrowth");
    }

    @Test
    @DisplayName("Only selected lands return and opponent lands remain in their graveyard")
    void returnsOnlySelectedLands() {
        Card selected = new Forest();
        Card unselected = new Mountain();
        Card opponentLand = new Forest();
        Card nonland = new VengefulRegrowth();
        harness.setGraveyard(player1, List.of(selected, unselected, nonland));
        harness.setGraveyard(player2, List.of(opponentLand));
        harness.castFromHand(player1, new VengefulRegrowth(), "{4}{G}{G}");
        choose(selected);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unselected, nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A removed target does not contribute to the number of tokens")
    void countsOnlyLandsActuallyReturned() {
        Card first = new Forest();
        Card second = new Mountain();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new VengefulRegrowth(), "{4}{G}{G}");
        choose(first, second);
        harness.setGraveyard(player1, List.of(first));
        harness.setExile(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).contains(first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Flashback creates no tokens when every target becomes illegal and still exiles the spell")
    void flashbackWithAllTargetsRemoved() {
        Card spell = new VengefulRegrowth();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(spell, land));
        addMana(6, 2);

        harness.castFlashback(player1, 0);
        choose(land);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(land));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void choose(Card... cards) {
        harness.handleMultipleCardsChosen(player1, List.of(cards).stream().map(Card::getId).toList());
    }

    private void addMana(int colorless, int green) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
    }
}

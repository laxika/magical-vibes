package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyTheHorde.class, InnerChamberGuard.class, OboroPalaceInTheClouds.class})
class RallyTheHordeTest extends BaseCardTest {

    @Test
    void exilesGroupsUntilTheLastCardIsALandAndCreatesTokensForNonlands() {
        List<Card> library = List.of(
                new InnerChamberGuard(), new OboroPalaceInTheClouds(), new InnerChamberGuard(),
                new InnerChamberGuard(), new InnerChamberGuard(), new OboroPalaceInTheClouds());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(4);
        assertThat(warriors).allSatisfy(warrior -> {
            assertThat(warrior.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(warrior.getCard().getPower()).isEqualTo(1);
            assertThat(warrior.getCard().getToughness()).isEqualTo(1);
            assertThat(warrior.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(warrior.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        });
    }

    @Test
    void repeatsWhenTheLastCardOfAFullGroupIsNonland() {
        List<Card> library = List.of(
                new OboroPalaceInTheClouds(), new InnerChamberGuard(), new InnerChamberGuard(),
                new OboroPalaceInTheClouds());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
    }

    @Test
    void stopsAfterAFullNonlandGroupWhenTheLibraryIsExhausted() {
        List<Card> library = List.of(
                new InnerChamberGuard(), new InnerChamberGuard(), new InnerChamberGuard());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(3);
    }

    @Test
    void exilesAllCardsWhenFewerThanThreeRemain() {
        List<Card> library = List.of(new InnerChamberGuard(), new InnerChamberGuard());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
    }

    @Test
    void doesNothingWhenTheLibraryIsEmpty() {
        List<Card> library = List.of();
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Warrior")).isZero();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new RallyTheHorde(), "{5}{R}");
        harness.passBothPriorities();
    }
}

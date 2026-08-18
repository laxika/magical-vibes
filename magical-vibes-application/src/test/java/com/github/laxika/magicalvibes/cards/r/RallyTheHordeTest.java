package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RallyTheHordeTest extends BaseCardTest {

    @Test
    void exilesGroupsUntilTheLastCardIsALandAndCreatesTokensForNonlands() {
        List<Card> library = List.of(
                new Shock(), new Forest(), new Shock(),
                new GrizzlyBears(), new Shock(), new Forest());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);

        List<Permanent> warriors = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Warrior"))
                .toList();
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
        List<Card> library = List.of(new Forest(), new Shock(), new Shock(), new Forest());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Warrior")))
                .hasSize(2);
    }

    @Test
    void exilesAllCardsWhenFewerThanThreeRemain() {
        List<Card> library = List.of(new Shock(), new GrizzlyBears());
        castWithLibrary(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Warrior")))
                .hasSize(2);
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new RallyTheHorde()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}

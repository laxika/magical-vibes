package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.o.Overture;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JidoorAristocraticCapital.class, Overture.class, Forest.class})
class JidoorAristocraticCapitalTest extends BaseCardTest {

    @Test
    @DisplayName("Jidoor enters tapped and produces blue mana")
    void entersTappedAndProducesBlueMana() {
        harness.setHand(player1, List.of(new JidoorAristocraticCapital()));

        harness.playLand(player1, 0);
        Permanent jidoor = findPermanent(player1, "Jidoor, Aristocratic Capital");
        assertThat(jidoor.isTapped()).isTrue();

        jidoor.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adventure mills half of an opponent's library rounded down and exiles Jidoor")
    void adventureMillsOpponentAndExilesLand() {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        harness.setHand(player1, List.of(jidoor));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(jidoor.getId()));

        harness.castFromExile(player1, jidoor.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(jidoor.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(jidoor.getId()));
        assertThat(findPermanent(player1, "Jidoor, Aristocratic Capital").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adventure cannot target its controller")
    void adventureCannotTargetController() {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        harness.setHand(player1, List.of(jidoor));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(jidoor);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 6})
    void adventureMillsHalfOfLibraryIncludingSmallLibraries(int librarySize) {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        List<Card> library = IntStream.range(0, librarySize)
                .mapToObj(i -> (Card) new Forest()).toList();
        harness.setHand(player1, List.of(jidoor));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        int milled = librarySize / 2;
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyElementsOf(library.subList(0, milled));
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(library.subList(milled, librarySize));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(jidoor);
    }

    @Test
    void adventureUsesLibrarySizeAtResolution() {
        harness.setHand(player1, List.of(new JidoorAristocraticCapital()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAdventure(player1, 0, player2.getId());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void playingLandFromAdventureExileRespectsLandPlayLimit() {
        JidoorAristocraticCapital jidoor = new JidoorAristocraticCapital();
        harness.setHand(player1, List.of(new Forest(), jidoor));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, jidoor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot play a land from exile now");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(jidoor);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(jidoor.getId()));
    }
}

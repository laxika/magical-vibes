package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spelunking;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpenTheWay.class, Forest.class, GrizzlyBears.class, Mountain.class})
class OpenTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals until X lands, puts them onto the battlefield tapped, and bottoms the rest")
    void revealsUntilXLandCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, bears, mountain));
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast with X greater than the number of players")
    void xCannotExceedNumberOfPlayers() {
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X can't be greater than 2");
    }

    @Test
    @DisplayName("X zero leaves the library unchanged")
    void zeroLeavesLibraryUnchanged() {
        List<Card> library = List.of(new Forest(), new GrizzlyBears(), new Mountain());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Open the Way");
    }

    @Test
    @DisplayName("Fewer than X lands puts every available land onto the battlefield")
    void fewerThanXLands() {
        Card firstNonland = new OpenTheWay();
        Card lastNonland = new OpenTheWay();
        harness.setLibrary(player1, List.of(firstNonland, new Forest(), lastNonland));
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, lastNonland);
    }

    @Test
    @DisplayName("A library with no lands retains all revealed cards")
    void noLandsRetainsLibraryCards() {
        List<Card> library = List.of(new OpenTheWay(), new OpenTheWay());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Stops at X lands and puts revealed nonlands below the untouched cards")
    void stopsAtXAndBottomsOnlyRevealedCards() {
        Card firstNonland = new OpenTheWay();
        Card secondNonland = new OpenTheWay();
        Card untouchedLand = new Mountain();
        Card untouchedNonland = new OpenTheWay();
        harness.setLibrary(player1, List.of(firstNonland, secondNonland, new Forest(),
                untouchedLand, untouchedNonland));
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Mountain");
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining).hasSize(4);
        assertThat(remaining.subList(0, 2)).containsExactly(untouchedLand, untouchedNonland);
        assertThat(remaining.subList(2, 4)).containsExactlyInAnyOrder(firstNonland, secondNonland);
    }

    @Test
    @DisplayName("An empty library resolves without putting any lands onto the battlefield")
    void emptyLibraryResolves() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Open the Way");
    }

    @Test
    @CardUsed({OpenTheWay.class, Forest.class, Mountain.class, Spelunking.class})
    @DisplayName("Spelunking makes the revealed lands enter untapped")
    void spelunkingMakesLandsEnterUntapped() {
        harness.addToBattlefield(player1, new Spelunking());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isFalse();
    }
}

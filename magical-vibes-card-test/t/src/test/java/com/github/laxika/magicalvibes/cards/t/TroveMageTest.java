package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TroveMage.class, GolemsHeart.class, GrizzlyBears.class, SteelHellkite.class})
class TroveMageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB seeks a random artifact from the top ten and shuffles")
    void seeksArtifactFromTopTen() {
        GolemsHeart topTenArtifact = new GolemsHeart();
        SteelHellkite outsideTopTenArtifact = new SteelHellkite();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            library.add(new GrizzlyBears());
        }
        library.add(topTenArtifact);
        library.add(outsideTopTenArtifact);
        harness.setLibrary(player1, library);
        castTroveMage();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topTenArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(10)
                .contains(outsideTopTenArtifact)
                .doesNotContain(topTenArtifact);
    }

    @Test
    @DisplayName("ETB does not seek an artifact below the top ten")
    void onlyConsidersTopTen() {
        GolemsHeart belowTopTenArtifact = new GolemsHeart();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new GrizzlyBears());
        }
        library.add(belowTopTenArtifact);
        harness.setLibrary(player1, library);
        castTroveMage();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(11)
                .contains(belowTopTenArtifact);
    }

    @Test
    @DisplayName("ETB seeks an artifact when the library has fewer than ten cards")
    void seeksFromShortLibrary() {
        SteelHellkite artifact = new SteelHellkite();
        GrizzlyBears nonartifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonartifact, artifact));
        castTroveMage();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonartifact);
    }

    @Test
    @DisplayName("ETB seeks exactly one card when multiple artifacts are eligible")
    void seeksExactlyOneArtifact() {
        GolemsHeart firstArtifact = new GolemsHeart();
        SteelHellkite secondArtifact = new SteelHellkite();
        GrizzlyBears nonartifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstArtifact, nonartifact, secondArtifact));
        castTroveMage();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .allSatisfy(card -> assertThat(card).isIn(firstArtifact, secondArtifact));
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .contains(nonartifact)
                .doesNotContain(gd.playerHands.get(player1.getId()).getFirst());
        List<Card> remainingCards = new ArrayList<>(gd.playerDecks.get(player1.getId()));
        remainingCards.addAll(gd.playerHands.get(player1.getId()));
        assertThat(remainingCards).containsExactlyInAnyOrder(firstArtifact, nonartifact, secondArtifact);
    }

    @Test
    @DisplayName("ETB resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castTroveMage();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Trove Mage");
    }

    @Test
    @DisplayName("Seeking waits for the ETB trigger and uses only its controller's library")
    void seeksOnlyWhenTriggerResolves() {
        GolemsHeart artifact = new GolemsHeart();
        SteelHellkite opponentArtifact = new SteelHellkite();
        harness.setLibrary(player1, List.of(artifact));
        harness.setLibrary(player2, List.of(opponentArtifact));
        harness.setHand(player2, List.of());
        castTroveMage();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trove Mage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentArtifact);
    }

    private void castTroveMage() {
        harness.castFromHand(player1, new TroveMage(), "{2}{U}");
    }
}

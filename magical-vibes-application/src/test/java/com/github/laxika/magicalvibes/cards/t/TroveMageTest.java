package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castTroveMage() {
        harness.setHand(player1, List.of(new TroveMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}

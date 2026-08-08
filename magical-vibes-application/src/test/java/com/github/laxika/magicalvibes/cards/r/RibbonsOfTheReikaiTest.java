package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RibbonsOfTheReikaiTest extends BaseCardTest {

    private void castRibbons() {
        harness.setLibrary(player1, new ArrayList<>(
                IntStream.range(0, 6).mapToObj(i -> (Card) new GrizzlyBears()).toList()));
        harness.setHand(player1, List.of(new RibbonsOfTheReikai()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Draws a card for each Spirit the caster controls")
    void drawsOnePerSpirit() {
        harness.addToBattlefield(player1, new KamiOfAncientLaw());
        harness.addToBattlefield(player1, new KamiOfAncientLaw());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws nothing when no Spirits are controlled")
    void drawsNothingWithoutSpirits() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Spirits controlled by the opponent are not counted")
    void ignoresOpponentSpirits() {
        harness.addToBattlefield(player1, new KamiOfAncientLaw());
        harness.addToBattlefield(player2, new KamiOfAncientLaw());
        harness.addToBattlefield(player2, new KamiOfAncientLaw());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}

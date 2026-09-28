package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boltfire.class, Counterspell.class})
class BoltfireTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenCastFromHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Boltfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Boltfire");
    }

    @Test
    void flashforwardCastsFromExileForItsAlternativeCostAndBottomsTheCard() {
        Boltfire bolt = new Boltfire();
        harness.setLife(player2, 20);
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
    }

    @Test
    void flashforwardCardCannotBeCastFromExileUsingItsNormalCost() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bolt.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void counteredFlashforwardSpellAlsoGoesToTheBottomOfItsOwnersLibrary() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
    }
}

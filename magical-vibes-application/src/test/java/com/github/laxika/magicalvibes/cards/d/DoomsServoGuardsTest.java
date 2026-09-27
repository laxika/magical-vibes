package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomsServoGuards.class, Forest.class, GrizzlyBears.class})
class DoomsServoGuardsTest extends BaseCardTest {

    @Test
    void entersGainsLifeAndMillsTwoCards() {
        List<Card> library = List.of(
                new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DoomsServoGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(2));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1));
    }

    @Test
    void millsOnlyCardsRemainingInShortLibrary() {
        List<Card> library = List.of(new Forest());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DoomsServoGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(library.get(0));
    }
}

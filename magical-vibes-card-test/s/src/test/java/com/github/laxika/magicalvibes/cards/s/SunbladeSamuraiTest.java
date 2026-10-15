package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunbladeSamurai.class, Plains.class, Forest.class})
class SunbladeSamuraiTest extends BaseCardTest {

    @Test
    void channelSearchesForABasicPlainsAndGainsLife() {
        harness.setHand(player1, List.of(new SunbladeSamurai()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunblade Samurai");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Plains", "Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Plains");
    }

    @Test
    void channelCanFailToFindEvenWhenAPlainsIsAvailable() {
        harness.setHand(player1, List.of(new SunbladeSamurai()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Sunblade Samurai");
        harness.assertNotInHand(player1, "Sunblade Samurai");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void channelGainsLifeWhenLibraryHasNoPlains() {
        harness.setHand(player1, List.of(new SunbladeSamurai()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunblade Samurai");
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void channelGainsLifeWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new SunbladeSamurai()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunblade Samurai");
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

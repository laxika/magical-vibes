package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyoshiIslandPlaza.class, Forest.class, Island.class, Plains.class})
class KyoshiIslandPlazaTest extends BaseCardTest {

    @Test
    void entersAndSearchesForUpToTheNumberOfShrinesYouControl() {
        harness.addToBattlefield(player1, shrine());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island()));

        harness.enterBattlefieldAndReturn(player1, new KyoshiIslandPlaza());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void anotherShrineEnteringSearchesForOneBasicLand() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, shrine());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void nonShrineEnchantmentEnteringDoesNotTriggerTheSearch() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.enterBattlefieldAndReturn(player1, enchantment());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }

    private Card enchantment() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        return card;
    }
}

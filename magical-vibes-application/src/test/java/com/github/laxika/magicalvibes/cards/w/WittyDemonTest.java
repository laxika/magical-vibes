package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WittyDemon.class, GrizzlyBears.class})
class WittyDemonTest extends BaseCardTest {

    @Test
    void searchesWhenStartingDeckHasThirteenCardsOverMinimum() {
        Card target = new GrizzlyBears();
        gd.startingDeckSizes.put(player1.getId(), 53);
        harness.setLibrary(player1, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void dealsDamageWhenStartingDeckIsNotLargeEnough() {
        gd.startingDeckSizes.put(player1.getId(), 52);

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }
}

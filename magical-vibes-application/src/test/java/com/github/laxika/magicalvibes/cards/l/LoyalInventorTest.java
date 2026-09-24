package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalInventor.class, DarksteelRelic.class, GrizzlyBears.class, RoyalAssassin.class})
class LoyalInventorTest extends BaseCardTest {

    @Test
    void withoutAnAssassinTheArtifactGoesOnTopOfTheLibrary() {
        DarksteelRelic relic = new DarksteelRelic();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonArtifact, relic));
        castLoyalInventor();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(relic);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(relic, nonArtifact);
    }

    @Test
    void withAnAssassinTheArtifactGoesToHand() {
        DarksteelRelic relic = new DarksteelRelic();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonArtifact, relic));
        harness.addToBattlefield(player1, new RoyalAssassin());
        castLoyalInventor();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(relic);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(relic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    void decliningTheMayAbilityDoesNothing() {
        DarksteelRelic relic = new DarksteelRelic();
        harness.setLibrary(player1, List.of(relic));
        castLoyalInventor();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(relic);
    }

    private void castLoyalInventor() {
        harness.setHand(player1, List.of(new LoyalInventor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

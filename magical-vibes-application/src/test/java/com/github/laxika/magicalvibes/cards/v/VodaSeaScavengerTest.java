package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodaSeaScavenger.class, GrizzlyBears.class, Plains.class, Island.class, Swamp.class})
class VodaSeaScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you put one of the Domain cards on top and randomly bottoms the rest")
    void etbPutsChosenCardOnTopAndRestOnBottom() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card first = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        Card last = new GrizzlyBears();
        castAndResolve(first, chosen, last);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last, chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("You may decline and all looked-at cards go to the bottom")
    void mayDecline() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card first = new GrizzlyBears();
        Card last = new GrizzlyBears();
        castAndResolve(first, last);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Domain counts duplicate basic land types only once")
    void duplicateBasicLandTypesCountOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card belowDomainCount = new GrizzlyBears();
        castAndResolve(first, second, belowDomainCount);

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        assertThat(search.params().cards()).doesNotContain(belowDomainCount);
    }

    private void castAndResolve(Card... libraryCards) {
        harness.setHand(player1, List.of(new VodaSeaScavenger()));
        harness.setLibrary(player1, List.of(libraryCards));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

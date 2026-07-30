package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeafCrownedElder;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescendantsPathTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature sharing a creature type offers the free cast")
    void sharedTypeCreatureOffersFreeCast() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new GrizzlyBears());
        setLibraryTop(new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Accepting casts the revealed creature without paying its mana cost")
    void acceptingCastsForFree() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new GrizzlyBears());
        setLibraryTop(new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free creature spell

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining puts the revealed card on the bottom of the library")
    void decliningBottomsTheCard() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new GrizzlyBears());
        Card top = new GrizzlyBears();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature sharing no creature type is bottomed with no choice")
    void unsharedTypeCreatureIsBottomed() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new GrizzlyBears()); // Bear
        Card top = new LeafCrownedElder(); // Treefolk Shaman — no shared type
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("A non-creature card is bottomed with no choice")
    void nonCreatureIsBottomed() {
        harness.addToBattlefield(player1, new DescendantsPath());
        addCreatureReady(player1, new GrizzlyBears());
        Card top = new LightningBolt();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("No creatures controlled means nothing is castable")
    void noCreaturesMeansNoFreeCast() {
        harness.addToBattlefield(player1, new DescendantsPath());
        Card top = new GrizzlyBears();
        setLibraryTop(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top);
    }

    private void setLibraryTop(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(card);
        for (int i = 0; i < 4; i++) {
            deck.add(new LightningBolt());
        }
    }
}

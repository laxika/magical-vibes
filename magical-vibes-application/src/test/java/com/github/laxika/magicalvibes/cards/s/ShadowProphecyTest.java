package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowProphecy.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class ShadowProphecyTest extends BaseCardTest {

    @Test
    @DisplayName("Domain 3 looks at three cards, keeps up to two, and puts the rest into the graveyard")
    void domainThreeKeepsTwoAndGraveyardsTheRest() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card first = new Mountain();
        Card second = new Forest();
        Card third = new Plains();
        setupDeck(first, second, third);

        castShadowProphecy();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.allCards()).containsExactly(first, second, third);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
        harness.assertInGraveyard(player1, "Shadow Prophecy");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Domain counts distinct basic land types and keeps the library card beyond the look count untouched")
    void domainCountsDistinctTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card first = new Mountain();
        Card second = new Forest();
        Card third = new Mountain();
        Card untouched = new Forest();
        setupDeck(first, second, third, untouched);

        castShadowProphecy();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    @DisplayName("With no basic land types, Shadow Prophecy only causes its life loss")
    void domainZeroDoesNotLookAtCards() {
        Card topCard = new Forest();
        setupDeck(topCard);

        castShadowProphecy();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shadow Prophecy");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("With one card looked at, it goes to hand without prompting")
    void fewerThanTwoCardsGoToHand() {
        harness.addToBattlefield(player1, new Plains());
        Card onlyCard = new Forest();
        setupDeck(onlyCard);

        castShadowProphecy();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void castShadowProphecy() {
        harness.setHand(player1, List.of(new ShadowProphecy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setupDeck(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

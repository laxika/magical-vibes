package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfNissa.class, Forest.class, GrizzlyBears.class, JaceBeleren.class, Shock.class})
class OathOfNissaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers creature, land, and planeswalker cards among the top three")
    void etbOffersCreatureLandAndPlaneswalker() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card planeswalker = new JaceBeleren();
        setupTopCards(List.of(creature, land, planeswalker));

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(creature.getId(), land.getId(), planeswalker.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB excludes cards that are not creatures, lands, or planeswalkers")
    void etbExcludesOtherCardTypes() {
        Card planeswalker = new JaceBeleren();
        Card land = new Forest();
        Card instant = new Shock();
        setupTopCards(List.of(planeswalker, land, instant));

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(planeswalker.getId(), land.getId());
        assertThat(choice.validCardIds()).doesNotContain(instant.getId());
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and bottoms the rest")
    void choosingCardPutsItIntoHandAndBottomsRest() {
        Card planeswalker = new JaceBeleren();
        Card land = new Forest();
        Card instant = new Shock();
        setupTopCards(List.of(planeswalker, land, instant));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(planeswalker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves all three cards on the bottom of the library")
    void decliningBottomsAllCards() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        setupTopCards(List.of(creature, land, instant));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature, land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows a planeswalker to be cast with only off-color mana")
    void castsPlaneswalkerWithOffColorMana() {
        harness.addToBattlefield(player1, new OathOfNissa());
        JaceBeleren planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), planeswalker,
                        gd.playerManaPools.get(player1.getId()), 0)).isTrue();

        harness.castPlaneswalker(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    @DisplayName("Does not allow off-color mana to cast a planeswalker without Oath of Nissa")
    void doesNotAllowOffColorPlaneswalkerWithoutOath() {
        JaceBeleren planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), planeswalker,
                        gd.playerManaPools.get(player1.getId()), 0)).isFalse();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new OathOfNissa()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}

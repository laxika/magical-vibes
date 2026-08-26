package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcclaimedContender.class, YouthfulKnight.class, GrizzlyBears.class})
class AcclaimedContenderTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger without another Knight")
    void doesNotTriggerWithoutAnotherKnight() {
        Card eligible = card("Knight card", CardType.CREATURE, CardSubtype.KNIGHT);
        harness.setLibrary(player1, List.of(eligible));
        harness.setHand(player1, List.of(new AcclaimedContender()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    @DisplayName("With another Knight, offers matching cards from the top five")
    void offersMatchingCardsWhenControllingAnotherKnight() {
        Card knight = card("Knight card", CardType.CREATURE, CardSubtype.KNIGHT);
        Card aura = card("Aura card", CardType.ENCHANTMENT, CardSubtype.AURA);
        Card equipment = card("Equipment card", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        Card legendaryArtifact = card("Legendary artifact", CardType.ARTIFACT);
        legendaryArtifact.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Card ineligible = card("Ineligible card", CardType.SORCERY);
        harness.setLibrary(player1, List.of(knight, aura, equipment, legendaryArtifact, ineligible));
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new AcclaimedContender()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                knight.getId(), aura.getId(), equipment.getId(), legendaryArtifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline and put all five looked-at cards on the bottom")
    void mayDecline() {
        Card knight = card("Knight card", CardType.CREATURE, CardSubtype.KNIGHT);
        Card aura = card("Aura card", CardType.ENCHANTMENT, CardSubtype.AURA);
        Card ineligible = card("Ineligible card", CardType.SORCERY);
        harness.setLibrary(player1, List.of(knight, aura, ineligible));
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new AcclaimedContender()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card == knight || card == aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(knight, aura, ineligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}

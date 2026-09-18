package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefinitelyNotATurtle.class, LightningBolt.class})
class DefinitelyNotATurtleTest extends BaseCardTest {

    @Test
    void deathTriggerOffersLandsAndLegendaryTurtles() {
        harness.setHand(player1, List.of());
        Card land = card("Top Land", CardType.LAND);
        Card legendaryTurtle = card("Legendary Turtle", CardType.CREATURE);
        legendaryTurtle.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        legendaryTurtle.setSubtypes(List.of(CardSubtype.TURTLE));
        Card ordinaryTurtle = card("Ordinary Turtle", CardType.CREATURE);
        ordinaryTurtle.setSubtypes(List.of(CardSubtype.TURTLE));
        Card legendaryCreature = card("Legendary Creature", CardType.CREATURE);
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Card artifact = card("Top Artifact", CardType.ARTIFACT);
        Card enchantment = card("Top Enchantment", CardType.ENCHANTMENT);
        List<Card> topCards = List.of(ordinaryTurtle, legendaryCreature, land,
                legendaryTurtle, artifact, enchantment);
        setLibrary(topCards);

        Permanent source = harness.addToBattlefieldAndReturn(player1, new DefinitelyNotATurtle());
        destroyWithLightningBolt(source);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), legendaryTurtle.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(legendaryTurtle.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(legendaryTurtle);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.stream()
                        .filter(card -> card != legendaryTurtle)
                        .toList());
    }

    @Test
    void decliningTheDeathTriggerPutsAllSixCardsOnTheBottom() {
        harness.setHand(player1, List.of());
        Card land = card("Top Land", CardType.LAND);
        Card legendaryTurtle = card("Legendary Turtle", CardType.CREATURE);
        legendaryTurtle.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        legendaryTurtle.setSubtypes(List.of(CardSubtype.TURTLE));
        List<Card> topCards = List.of(land, legendaryTurtle,
                card("Top One", CardType.CREATURE), card("Top Two", CardType.ARTIFACT),
                card("Top Three", CardType.ENCHANTMENT), card("Top Four", CardType.SORCERY));
        setLibrary(topCards);

        Permanent source = harness.addToBattlefieldAndReturn(player1, new DefinitelyNotATurtle());
        destroyWithLightningBolt(source);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void withNoMatchTheDeathTriggerPutsAllCardsOnTheBottomWithoutPrompting() {
        harness.setHand(player1, List.of());
        List<Card> topCards = List.of(
                card("Top One", CardType.CREATURE), card("Top Two", CardType.ARTIFACT),
                card("Top Three", CardType.ENCHANTMENT), card("Top Four", CardType.SORCERY),
                card("Top Five", CardType.INSTANT), card("Top Six", CardType.BATTLE));
        setLibrary(topCards);

        Permanent source = harness.addToBattlefieldAndReturn(player1, new DefinitelyNotATurtle());
        destroyWithLightningBolt(source);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void destroyWithLightningBolt(Permanent source) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}

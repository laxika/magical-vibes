package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AprilONeilHumanElement;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.g.GameOver;
import com.github.laxika.magicalvibes.cards.l.LeonardoWorldlyWarrior;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefinitelyNotATurtle.class, LightningBolt.class, GameOver.class, CommandTower.class,
        LeonardoWorldlyWarrior.class, AprilONeilHumanElement.class})
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
        harness.setLibrary(player1, topCards);

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
        harness.setLibrary(player1, topCards);

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
        harness.setLibrary(player1, topCards);

        Permanent source = harness.addToBattlefieldAndReturn(player1, new DefinitelyNotATurtle());
        destroyWithLightningBolt(source);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void onlyTheTopSixAreEligibleAndTheRestGoBelowUntouchedCards() {
        CommandTower land = new CommandTower();
        LeonardoWorldlyWarrior legendaryTurtle = new LeonardoWorldlyWarrior();
        AprilONeilHumanElement legendaryNonTurtle = new AprilONeilHumanElement();
        DefinitelyNotATurtle ordinaryTurtle = new DefinitelyNotATurtle();
        List<Card> topCards = List.of(land, legendaryTurtle, legendaryNonTurtle,
                ordinaryTurtle, new GameOver(), new GameOver());
        CommandTower seventhCard = new CommandTower();
        LeonardoWorldlyWarrior eighthCard = new LeonardoWorldlyWarrior();
        harness.setLibrary(player1, List.of(land, legendaryTurtle, legendaryNonTurtle,
                ordinaryTurtle, topCards.get(4), topCards.get(5), seventhCard, eighthCard));

        resolveDeathWithGameOver();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), legendaryTurtle.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(seventhCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(land.getId(), legendaryTurtle.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.subList(0, 2)).containsExactly(seventhCard, eighthCard);
        assertThat(library.subList(2, 7)).containsExactlyInAnyOrderElementsOf(topCards.subList(1, 6));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryStillAllowsTakingALegendaryTurtle() {
        LeonardoWorldlyWarrior turtle = new LeonardoWorldlyWarrior();
        AprilONeilHumanElement nonTurtle = new AprilONeilHumanElement();
        harness.setLibrary(player1, List.of(nonTurtle, turtle));

        resolveDeathWithGameOver();
        harness.handleMultipleCardsChosen(player1, List.of(turtle.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(turtle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonTurtle);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void takingTheOnlyCardLeavesTheLibraryEmpty() {
        CommandTower land = new CommandTower();
        harness.setLibrary(player1, List.of(land));

        resolveDeathWithGameOver();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void takingTheOnlyEligibleCardIsOptionalEvenInASingleCardLibrary() {
        CommandTower land = new CommandTower();
        harness.setLibrary(player1, List.of(land));

        resolveDeathWithGameOver();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoiceOrDrawingACard() {
        harness.setLibrary(player1, List.of());

        resolveDeathWithGameOver();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveDeathWithGameOver() {
        harness.addToBattlefield(player1, new DefinitelyNotATurtle());
        harness.castFromHand(player1, new GameOver(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithLightningBolt(Permanent source) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}

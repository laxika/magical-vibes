package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.q.QuestingBeast;
import com.github.laxika.magicalvibes.cards.s.ShiningArmor;
import com.github.laxika.magicalvibes.cards.s.SilverflameRitual;
import com.github.laxika.magicalvibes.cards.t.TheGreatHenge;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcclaimedContender.class, YouthfulKnight.class, AllThatGlitters.class,
        ShiningArmor.class, TheGreatHenge.class, SilverflameRitual.class,
        Gingerbrute.class, QuestingBeast.class})
class AcclaimedContenderTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger without another Knight")
    void doesNotTriggerWithoutAnotherKnight() {
        Card eligible = card("Knight card", CardType.CREATURE, CardSubtype.KNIGHT);
        harness.setLibrary(player1, List.of(eligible));
        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
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
        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                knight.getId(), aura.getId(), equipment.getId(), legendaryArtifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline and put all looked-at cards on the bottom")
    void mayDecline() {
        Card knight = card("Knight card", CardType.CREATURE, CardSubtype.KNIGHT);
        Card aura = card("Aura card", CardType.ENCHANTMENT, CardSubtype.AURA);
        Card ineligible = card("Ineligible card", CardType.SORCERY);
        harness.setLibrary(player1, List.of(knight, aura, ineligible));
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card == knight || card == aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(knight, aura, ineligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Can take each eligible category, leaving untouched cards above the random remainder")
    void takesEachEligibleCategory(int selectedIndex) {
        List<Card> lookedAt = List.of(new YouthfulKnight(), new AllThatGlitters(),
                new ShiningArmor(), new TheGreatHenge(), new SilverflameRitual());
        Card untouched = new YouthfulKnight();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                lookedAt.get(0).getId(), lookedAt.get(1).getId(),
                lookedAt.get(2).getId(), lookedAt.get(3).getId());
        Card selected = lookedAt.get(selectedIndex);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("puts " + selected.getName()
                + " into their hand"));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 5)).containsExactlyInAnyOrderElementsOf(
                lookedAt.stream().filter(card -> card != selected).toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline even when a short library has only one eligible card")
    void mayDeclineOnlyEligibleCard() {
        Card knight = new YouthfulKnight();
        harness.setLibrary(player1, List.of(knight));
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(knight);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ordinary artifacts and legendary nonartifacts are not eligible")
    void rejectsOrdinaryArtifactsAndLegendaryNonartifacts() {
        List<Card> lookedAt = List.of(new Gingerbrute(), new QuestingBeast(),
                new SilverflameRitual(), new SilverflameRitual(), new Gingerbrute());
        Card untouched = new YouthfulKnight();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Knight does not satisfy the condition")
    void opponentsKnightDoesNotEnableTrigger() {
        Card knight = new YouthfulKnight();
        harness.setLibrary(player1, List.of(knight));
        harness.addToBattlefield(player2, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(knight);
    }

    @Test
    @DisplayName("Condition is checked again when the trigger resolves")
    void doesNothingWhenOtherKnightLeavesBeforeResolution() {
        Card knight = new YouthfulKnight();
        harness.setLibrary(player1, List.of(knight));
        var otherKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(otherKnight);
        gd.playerGraveyards.get(player1.getId()).add(otherKnight.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(knight);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trigger still resolves after Contender leaves if another Knight remains")
    void resolvesAfterSourceLeaves() {
        Card knight = new YouthfulKnight();
        AcclaimedContender contender = new AcclaimedContender();
        harness.setLibrary(player1, List.of(knight));
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.castFromHand(player1, contender, "{2}{W}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == contender);
        gd.playerGraveyards.get(player1.getId()).add(contender);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(knight.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(knight);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.castFromHand(player1, new AcclaimedContender(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}

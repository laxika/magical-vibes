package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Candletrap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormTheFestival.class, GrizzlyBears.class, MindStone.class, SerraAngel.class,
        Shock.class, ShivanDragon.class, Forest.class, Candletrap.class})
class StormTheFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two permanent cards with mana value five or less onto the battlefield")
    void putsUpToTwoEligiblePermanentsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card angel = new SerraAngel();
        Card shock = new Shock();
        Card dragon = new ShivanDragon();
        setLibrary(bears, mindStone, angel, shock, dragon);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), mindStone.getId(), angel.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), mindStone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, shock, dragon);
    }

    @Test
    @DisplayName("Flashback exiles Storm the Festival after resolving")
    void flashbackExilesAfterResolving() {
        Card bears = new GrizzlyBears();
        setLibrary(bears, new Shock());
        harness.setGraveyard(player1, List.of(new StormTheFestival()));
        addMana(7, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Storm the Festival");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Storm the Festival"));
    }

    @Test
    void mayDeclineAllEligibleCardsAndLeavesUnlookedCardsOnTop() {
        Card forest = new Forest();
        Card secondForest = new Forest();
        Card thirdForest = new Forest();
        Card fourthForest = new Forest();
        Card fifthForest = new Forest();
        Card sixthForest = new Forest();
        List<Card> lookedAt = List.of(forest, secondForest, thirdForest, fourthForest, fifthForest);
        setLibrary(forest, secondForest, thirdForest, fourthForest, fifthForest, sixthForest);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                lookedAt.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixthForest);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Storm the Festival");
    }

    @Test
    void mayPutOneLandOntoBattlefieldFromAShortLibrary() {
        Card forest = new Forest();
        setLibrary(forest);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Storm the Festival");
    }

    @Test
    void resolvesWithoutAChoiceWhenNoCardsAreEligible() {
        Card shock = new Shock();
        Card dragon = new ShivanDragon();
        setLibrary(shock, dragon);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(shock, dragon);
        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        harness.assertInGraveyard(player1, "Storm the Festival");
    }

    @Test
    void manaValueLimitAppliesToEachSelectedCardRatherThanTheirCombinedValue() {
        Card angel = new SerraAngel();
        Card bears = new GrizzlyBears();
        setLibrary(angel, bears);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        setLibrary();
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Storm the Festival");
    }

    @Test
    void auraSelectedWithAnotherPermanentEntersAttachedToAnExistingCreature() {
        var host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Candletrap();
        Card forest = new Forest();
        setLibrary(aura, forest);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Candletrap");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(aura);
                    assertThat(permanent.getAttachedTo()).isEqualTo(host.getId());
                });
    }

    @Test
    void auraWithoutALegalHostSelectedWithALandRemainsInTheLibrary() {
        Card aura = new Candletrap();
        Card forest = new Forest();
        setLibrary(aura, forest);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Candletrap");
        harness.assertNotInGraveyard(player1, "Candletrap");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
    }

    private void addMana(int colorless, int green) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}

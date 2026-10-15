package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HeroOfBretagard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzledHuntmaster.class, GrizzlyBears.class, Forest.class, HeroOfBretagard.class})
class GrizzledHuntmasterTest extends BaseCardTest {

    @Test
    void exilesSameNamedCardsAndConjuresOneDuplicatePerHandCardExiled() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzlyBears additionalHandCopy = new GrizzlyBears();
        GrizzlyBears libraryCopy = new GrizzlyBears();
        GrizzlyBears outsideCreature = new GrizzlyBears();

        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand, additionalHandCopy));
        harness.setLibrary(player1, List.of(new Forest(), libraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.RevealedHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(handChoice).isNotNull();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenFromHand));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(additionalHandCopy.getId(), libraryCopy.getId()));

        PendingInteraction.SearchOutsideGameOrExileCardChoice outsideChoice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(outsideChoice).isNotNull();
        assertThat(outsideChoice.mandatory()).isTrue();
        assertThat(outsideChoice.validCardIds()).containsExactly(outsideCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenFromHand, additionalHandCopy, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))).hasSize(2);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineTheInitialCreatureExile() {
        GrizzlyBears creature = new GrizzlyBears();
        GrizzlyBears outsideCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), creature));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayExileNoAdditionalCopiesAndChooseADifferentOutsideCreature() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzlyBears retainedHandCopy = new GrizzlyBears();
        GrizzlyBears retainedLibraryCopy = new GrizzlyBears();
        GrizzledHuntmaster outsideCreature = new GrizzledHuntmaster();
        Forest outsideLand = new Forest();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand, retainedHandCopy));
        harness.setLibrary(player1, List.of(retainedLibraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature, outsideLand)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        PendingInteraction.SearchOutsideGameOrExileCardChoice outsideChoice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(outsideChoice).isNotNull();
        assertThat(outsideChoice.validCardIds()).containsExactly(outsideCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenFromHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retainedLibraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(retainedHandCopy)
                .doesNotContain(outsideCreature, chosenFromHand);
        harness.assertInHand(player1, "Grizzled Huntmaster");
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature, outsideLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void libraryCopiesDoNotIncreaseTheNumberOfConjuredCards() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzlyBears libraryCopy = new GrizzlyBears();
        GrizzledHuntmaster outsideCreature = new GrizzledHuntmaster();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand));
        harness.setLibrary(player1, List.of(libraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(libraryCopy.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(chosenFromHand, libraryCopy);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).doesNotContain(outsideCreature);
        harness.assertInHand(player1, "Grizzled Huntmaster");
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void conjuresOneCardWhenNoOtherSameNamedCardsExist() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzledHuntmaster outsideCreature = new GrizzledHuntmaster();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenFromHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).doesNotContain(outsideCreature);
        harness.assertInHand(player1, "Grizzled Huntmaster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNothingWhenTheHandContainsNoCreatureCard() {
        Forest handCard = new Forest();
        GrizzlyBears libraryCard = new GrizzlyBears();
        GrizzlyBears outsideCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void stillExilesCardsWhenNoOutsideCreatureIsAvailable() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzlyBears libraryCopy = new GrizzlyBears();
        Forest outsideLand = new Forest();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand));
        harness.setLibrary(player1, List.of(libraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideLand)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(chosenFromHand, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void initialHandExileTriggersHeroOfBretagard() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBretagard());
        GrizzledHuntmaster chosenFromHand = new GrizzledHuntmaster();
        GrizzledHuntmaster outsideCreature = new GrizzledHuntmaster();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand));
        harness.setLibrary(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenFromHand);
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void additionalHandExilesTriggerHeroOfBretagardButLibraryExilesDoNot() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBretagard());
        GrizzledHuntmaster chosenFromHand = new GrizzledHuntmaster();
        GrizzledHuntmaster additionalHandCopy = new GrizzledHuntmaster();
        GrizzledHuntmaster libraryCopy = new GrizzledHuntmaster();
        GrizzledHuntmaster outsideCreature = new GrizzledHuntmaster();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand, additionalHandCopy));
        harness.setLibrary(player1, List.of(libraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(additionalHandCopy.getId(), libraryCopy.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenFromHand, additionalHandCopy, libraryCopy);
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

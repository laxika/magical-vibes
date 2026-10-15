package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TheGrandEvolution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VorinclexTheGrandEvolution.class, TheGrandEvolution.class,
        Forest.class, GrizzlyBears.class, HillGiant.class})
class VorinclexTheGrandEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("When Vorinclex enters, it searches for up to two Forests")
    void searchesForForests() {
        harness.setHand(player1, List.of(new VorinclexTheGrandEvolution()));
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card instanceof Forest);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    @Test
    @DisplayName("Chapter I offers only creature cards milled by this chapter and returns the chosen cards")
    void chapterIMillsAndReturnsChosenCreatures() {
        Permanent saga = addBackFaceSaga(0);
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                first, new Forest(), second, new Forest(), third,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId())
                .doesNotContain(third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(third.getId());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III returns the Saga to its front face")
    void chapterIIIReturnsFrontFace() {
        addBackFaceSaga(2);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.isTransformed()
                        && permanent.getOriginalCard() instanceof VorinclexTheGrandEvolution);
    }

    @Test
    void activationReturnsTransformedAndImmediatelyTriggersChapterI() {
        Permanent front = harness.addToBattlefieldAndReturn(player1, new VorinclexTheGrandEvolution());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(saga.getId()).isNotEqualTo(front.getId());
        assertThat(saga.isTransformed()).isTrue();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void activationCannotBeUsedDuringCombat() {
        harness.addToBattlefield(player1, new VorinclexTheGrandEvolution());
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterICanDeclineAllCreaturesAndDoesNotOfferExistingGraveyardCards() {
        addBackFaceSaga(0);
        Card oldCreature = new HillGiant();
        Card milledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(milledCreature, new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(milledCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(oldCreature.getId(), milledCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIIDistributesSevenCountersAsChosen() {
        addBackFaceSaga(1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        advanceSagaToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "5");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void chapterIIOffersOnlyCreaturesYouControl() {
        addBackFaceSaga(1);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        advanceSagaToNextChapter();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(own.getId()).doesNotContain(opposing.getId());
    }

    @Test
    void chapterIIIReturnsUnderChapterControllersControlRatherThanOwnersControl() {
        Permanent saga = addBackFaceSaga(2);
        saga.getOriginalCard().setOwnerId(player2.getId());

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.isTransformed()
                        && permanent.getOriginalCard() instanceof VorinclexTheGrandEvolution);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void chapterIIIGrantsFightToExistingCreaturesButNotReturnedVorinclexOrLaterCreatures() {
        addBackFaceSaga(2);
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);

        int fighterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fighter);
        harness.activateAbility(player1, fighterIndex, null, opposing.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(fighter.getMarkedDamage()).isEqualTo(2);

        Permanent late = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nextOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lateIndex = gd.playerBattlefields.get(player1.getId()).indexOf(late);
        assertThatThrownBy(() -> harness.activateAbility(player1, lateIndex, null, nextOpponent.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof VorinclexTheGrandEvolution)
                .findFirst().orElseThrow();
        int returnedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(returned);
        assertThatThrownBy(() -> harness.activateAbility(player1, returnedIndex, 1, null, nextOpponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void forestSearchCanFindNoCardsEvenWhenForestsAreAvailable() {
        harness.setHand(player1, List.of(new VorinclexTheGrandEvolution()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void grantedFightCannotTargetYourOwnCreatureAndExpiresAtEndOfTurn() {
        addBackFaceSaga(2);
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        int fighterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fighter);

        assertThatThrownBy(() -> harness.activateAbility(player1, fighterIndex, null, own.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, fighterIndex, null, opposing.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fighter.getMarkedDamage()).isZero();
        assertThat(opposing.getMarkedDamage()).isZero();
    }
    private Permanent addBackFaceSaga(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new VorinclexTheGrandEvolution());
        saga.setCard(saga.getOriginalCard().getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

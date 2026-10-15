package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverSongsDiary.class, Shock.class, CounselOfTheSoratami.class})
class RiverSongsDiaryTest extends BaseCardTest {

    @Test
    @DisplayName("exiles a hand-cast instant from any player and tracks it with the Diary")
    void exilesHandCastInstantWithDiary() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(shock);
        assertThat(gd.getCardsExiledByPermanent(diary.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("at four exiled cards the upkeep trigger offers one random card for free")
    void upkeepOffersRandomExiledCardAtFour() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        List<Card> exiled = List.of(
                new CounselOfTheSoratami(),
                new CounselOfTheSoratami(),
                new CounselOfTheSoratami(),
                new CounselOfTheSoratami());
        exiled.forEach(card -> gd.addToExile(player1.getId(), card, diary.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(diary.getId())).hasSize(3);
        resolveAllTriggers();
    }

    @Test
    void upkeepDoesNotTriggerWithOnlyThreeExiledCards() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        for (int i = 0; i < 3; i++) {
            gd.addToExile(player1.getId(), new CounselOfTheSoratami(), diary.getId());
        }

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void upkeepDoesNothingIfExiledCountDropsBelowFourBeforeResolution() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        for (int i = 0; i < 4; i++) {
            gd.addToExile(player1.getId(), new CounselOfTheSoratami(), diary.getId());
        }
        advanceToUpkeep(player1);
        Card removed = gd.getCardsExiledByPermanent(diary.getId()).getFirst();
        gd.removeFromExile(removed.getId());
        gd.playerGraveyards.get(player1.getId()).add(removed);

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(diary.getId())).hasSize(3);
    }

    @Test
    void decliningRandomCardLeavesAllCardsExiled() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        for (int i = 0; i < 4; i++) {
            gd.addToExile(player1.getId(), new CounselOfTheSoratami(), diary.getId());
        }
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(diary.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        for (int i = 0; i < 4; i++) {
            gd.addToExile(player1.getId(), new CounselOfTheSoratami(), diary.getId());
        }

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void handCastSorceryResolvesBeforeBeingExiled() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, spell, "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.getCardsExiledByPermanent(diary.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void canCastOpponentsSorceryForFreeWithoutExilingItAgain() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new RiverSongsDiary());
        for (int i = 0; i < 4; i++) {
            gd.addToExile(player2.getId(), new CounselOfTheSoratami(), diary.getId());
        }
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(diary.getId())).hasSize(3);
    }
}

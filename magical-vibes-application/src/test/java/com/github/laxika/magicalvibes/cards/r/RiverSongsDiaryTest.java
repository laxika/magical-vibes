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
}

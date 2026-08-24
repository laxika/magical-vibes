package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomCarriage.class, CacklingCounterpart.class, BaithookAngler.class, HookHauntDrifter.class,
        GrizzlyBears.class})
class PhantomCarriageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search offers cards with flashback or disturb")
    void searchOffersFlashbackOrDisturbCards() {
        castPhantomCarriage();
        harness.setLibrary(player1, List.of(new CacklingCounterpart(), new BaithookAngler(), new GrizzlyBears()));

        resolveCarriageAndAcceptSearch();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactly("Cackling Counterpart", "Baithook Angler");
    }

    @Test
    @DisplayName("Choosing a matching card puts it into the graveyard")
    void chosenMatchingCardGoesToGraveyard() {
        castPhantomCarriage();
        Card disturbCard = new BaithookAngler();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), disturbCard));

        resolveCarriageAndAcceptSearch();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Baithook Angler");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search does not move a card")
    void decliningSearchDoesNothing() {
        castPhantomCarriage();
        harness.setLibrary(player1, List.of(new CacklingCounterpart()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPhantomCarriage() {
        harness.setHand(player1, List.of(new PhantomCarriage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveCarriageAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}

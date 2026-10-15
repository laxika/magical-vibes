package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DauntlessScrapbot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MechanNavigator.class, Forest.class, DauntlessScrapbot.class})
class MechanNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped draws a card, then discards a card")
    void becomingTappedDrawsThenDiscards() {
        Card discarded = new DauntlessScrapbot();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        Permanent navigator = addCreatureReady(player1, new MechanNavigator());

        tapAndResolve(navigator);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Dauntless Scrapbot");
        harness.assertInHand(player1, "Forest");
        assertThat(navigator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Mechan Navigator")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new MechanNavigator());
        Permanent scrapbot = addCreatureReady(player1, new DauntlessScrapbot());

        tapAndCheckNoTrigger(scrapbot);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty hand, the drawn card must be discarded")
    void emptyHandDiscardsTheDrawnCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent navigator = addCreatureReady(player1, new MechanNavigator());

        tapAndResolve(navigator);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Attacking triggers the draw and discard ability")
    void attackingTriggersAbility() {
        harness.setHand(player1, List.of(new MechanNavigator()));
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MechanNavigator());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mechan Navigator");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Each Navigator triggers only for its own tap")
    void secondNavigatorDoesNotDuplicateTrigger() {
        harness.setHand(player1, List.of(new MechanNavigator()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent navigator = addCreatureReady(player1, new MechanNavigator());
        addCreatureReady(player1, new MechanNavigator());

        tapAndCheckNoTrigger(navigator);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Mechan Navigator");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void tapAndResolve(Permanent permanent) {
        tapAndCheckNoTrigger(permanent);
        resolveAllTriggers();
    }

    private void tapAndCheckNoTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}

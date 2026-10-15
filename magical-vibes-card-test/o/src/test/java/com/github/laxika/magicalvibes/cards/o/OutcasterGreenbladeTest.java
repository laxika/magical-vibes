package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BristlingBackwoods;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutcasterGreenblade.class, Forest.class, BristlingBackwoods.class})
class OutcasterGreenbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering searches for a basic land or Desert and puts it into hand")
    void enteringSearchesForBasicLandOrDesert() {
        Forest forest = new Forest();
        BristlingBackwoods desert = new BristlingBackwoods();
        harness.setLibrary(player1, List.of(forest, desert, new OutcasterGreenblade()));
        harness.castFromHand(player1, new OutcasterGreenblade(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().cards()).contains(forest, desert);
        assertThat(search.params().cards()).allMatch(card -> card == forest || card == desert);

        Card chosen = search.params().cards().stream()
                .filter(card -> card == desert)
                .findFirst()
                .orElseThrow();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(chosen)));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosen);
        assertThat(gameLogContains("reveals Bristling Backwoods")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gets +1/+1 for each Desert its controller controls")
    void getsPlusOneForEachControlledDesert() {
        Permanent greenblade = harness.addToBattlefieldAndReturn(player1, new OutcasterGreenblade());
        harness.addToBattlefield(player1, new BristlingBackwoods());
        harness.addToBattlefield(player1, new BristlingBackwoods());
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Deserts controlled by an opponent do not contribute")
    void opponentDesertsDoNotContribute() {
        Permanent greenblade = harness.addToBattlefieldAndReturn(player1, new OutcasterGreenblade());
        harness.addToBattlefield(player2, new BristlingBackwoods());
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(2);
    }

    @Test
    void canChooseBasicLandInsteadOfDesert() {
        Forest forest = new Forest();
        BristlingBackwoods desert = new BristlingBackwoods();
        harness.setLibrary(player1, List.of(forest, desert));
        harness.castFromHand(player1, new OutcasterGreenblade(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(forest)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(desert);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWithEligibleCards() {
        Forest forest = new Forest();
        BristlingBackwoods desert = new BristlingBackwoods();
        harness.setLibrary(player1, List.of(forest, desert));
        harness.castFromHand(player1, new OutcasterGreenblade(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, desert);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoEligibleCardsCompletesWithoutMovingCards() {
        OutcasterGreenblade otherCreature = new OutcasterGreenblade();
        harness.setLibrary(player1, List.of(otherCreature));
        harness.castFromHand(player1, new OutcasterGreenblade(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bonusUpdatesAsControlledDesertsChange() {
        Permanent greenblade = harness.addToBattlefieldAndReturn(player1, new OutcasterGreenblade());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(2);

        Permanent desert = harness.addToBattlefieldAndReturn(player1, new BristlingBackwoods());
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(desert);
        gd.playerBattlefields.get(player2.getId()).add(desert);
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(2);
    }
}

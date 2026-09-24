package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
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

@CardUsed({ShieldWallSentinel.class, WallOfGranite.class, GrizzlyBears.class})
class ShieldWallSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search offers only creature cards with defender")
    void searchOffersOnlyCreaturesWithDefender() {
        castSentinel();
        harness.setLibrary(player1, List.of(new WallOfGranite(), new GrizzlyBears()));

        resolveSentinelAndAcceptSearch();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Wall of Granite");
    }

    @Test
    @DisplayName("Choosing a creature with defender puts it into hand")
    void chosenCreatureWithDefenderGoesToHand() {
        castSentinel();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WallOfGranite()));

        resolveSentinelAndAcceptSearch();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Wall of Granite");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search does not move a card")
    void decliningSearchDoesNothing() {
        castSentinel();
        harness.setLibrary(player1, List.of(new WallOfGranite()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSentinel() {
        harness.setHand(player1, List.of(new ShieldWallSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveSentinelAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}

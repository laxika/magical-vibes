package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabarooTroop.class, Forest.class, Plains.class})
class RabarooTroopTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall grants flying until end of turn and gains 1 life")
    void landfallGrantsFlyingAndGainsLife() {
        Permanent rabaroo = harness.addToBattlefieldAndReturn(player1, new RabarooTroop());
        harness.setHand(player1, List.of(new Forest()));

        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Rabaroo Troop")
    void opponentLandDoesNotTrigger() {
        Permanent rabaroo = harness.addToBattlefieldAndReturn(player1, new RabarooTroop());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Plainscycling discards Rabaroo Troop and searches for a Plains")
    void plainscyclingSearchesForPlains() {
        harness.setHand(player1, List.of(new RabarooTroop()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rabaroo Troop");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Plains");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
    }
}

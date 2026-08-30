package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBindingOfTheTitans.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheBindingOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three cards from each player")
    void chapterIMillsEachPlayer() {
        List<Card> controllerCards = List.of(new Forest(), new Forest(), new Forest());
        List<Card> opponentCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, controllerCards);
        harness.setLibrary(player2, opponentCards);
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(controllerCards);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(opponentCards);
    }

    @Test
    @DisplayName("Chapter II exiles up to two cards from any graveyards and gains life for creatures")
    void chapterIIExilesCardsAndGainsLifeForCreatureCards() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(land));
        gd.playerLifeTotals.put(player1.getId(), 20);
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Chapter III returns a creature or land from the graveyard to hand")
    void chapterIIIReturnsCreatureOrLandToHand() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Shock instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, land, instant));
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotOnBattlefield(player1, "The Binding of the Titans");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBindingOfTheTitans());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

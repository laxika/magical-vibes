package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekNewKnowledge.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class,
        LurkerInTheDeep.class})
class SeekNewKnowledgeTest extends BaseCardTest {

    @Test
    void seeksTwoNonlandCardsThenPutsAHandCardOnBottom() {
        Card soughtCreature = new GrizzlyBears();
        Card soughtSpell = new Shock();
        Card cardToBottom = new Shock();
        Card forest = new Forest();
        Card island = new Island();
        harness.setHand(player1, List.of(new SeekNewKnowledge(), cardToBottom));
        harness.setLibrary(player1, List.of(forest, soughtCreature, island, soughtSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(cardToBottom, soughtCreature, soughtSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, island);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> assertThat(choice.maxCount()).isEqualTo(1));

        harness.handleMultipleCardsChosen(player1, List.of(cardToBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(soughtCreature, soughtSpell);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(cardToBottom);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void seeksOnlyAvailableNonlandCards() {
        Card soughtCreature = new GrizzlyBears();
        Card cardToBottom = new Shock();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setHand(player1, List.of(new SeekNewKnowledge(), cardToBottom));
        harness.setLibrary(player1, List.of(forest, soughtCreature, island));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(cardToBottom, soughtCreature);
        harness.handleMultipleCardsChosen(player1, List.of(cardToBottom.getId()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(forest, island, cardToBottom);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillPutsAHandCardOnBottomWhenNoNonlandCardsAreAvailable() {
        Island cardToBottom = new Island();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SeekNewKnowledge(), cardToBottom));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(cardToBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, cardToBottom);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenSeekingFailsAndHandIsEmpty() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.castFromHand(player1, new SeekNewKnowledge(), "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutTheOnlySoughtCardBackIntoAnEmptyLibrary() {
        SeekNewKnowledge sought = new SeekNewKnowledge();
        harness.setLibrary(player1, List.of(sought));
        harness.castFromHand(player1, new SeekNewKnowledge(), "{1}{U}");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(sought.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SeekNewKnowledge.class, LurkerInTheDeep.class})
    void seekingTriggersLurkerForBothCardsEvenWhenOneIsPutBack() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new LurkerInTheDeep());
        SeekNewKnowledge first = new SeekNewKnowledge();
        SeekNewKnowledge second = new SeekNewKnowledge();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new SeekNewKnowledge(), "{1}{U}");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isManifested()).toList()).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getId())
                        .isNotIn(first.getId(), second.getId()));
    }
}

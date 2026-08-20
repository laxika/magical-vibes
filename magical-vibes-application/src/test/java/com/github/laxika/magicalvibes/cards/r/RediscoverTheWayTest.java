package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RediscoverTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I puts one of the top three cards into hand and orders the rest on the bottom")
    void chapterISelectsOneTopCard() {
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(elves, shock, plains));
        harness.setHand(player1, List.of(new RediscoverTheWay()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, elves);
    }

    @Test
    @DisplayName("Chapter II uses the same top-three selection")
    void chapterIISelectsOneTopCard() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RediscoverTheWay());
        saga.setCounterCount(CounterType.LORE, 1);
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(elves, shock, plains));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, plains);
    }

    @Test
    @DisplayName("Chapter III keeps triggering after the Saga leaves and targets only your creatures")
    void chapterIIITargetsControlledCreatureAfterSagaLeaves() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RediscoverTheWay());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rediscover the Way");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III does not trigger for a creature spell")
    void chapterIIIDoesNotTriggerForCreatureSpell() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RediscoverTheWay());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.hasKeyword(gd, ownCreature, com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void addSagaMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}

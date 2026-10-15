package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
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

@CardUsed({RediscoverTheWay.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, Shock.class})
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
        harness.castAndResolveInstant(player1, 0, player2.getId());

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

    @Test
    @DisplayName("Chapter I puts the only remaining library card into hand without a choice")
    void chapterIWithOneLibraryCard() {
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setHand(player1, List.of(new RediscoverTheWay()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter I with an empty library does nothing and does not cause a draw loss")
    void chapterIWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RediscoverTheWay()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Rediscover the Way");
    }

    @Test
    @DisplayName("Chapter I leaves cards below the top three above the ordered bottom cards")
    void chapterIPreservesUnseenCards() {
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card plains = new Plains();
        Card unseen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(elves, shock, plains, unseen));
        harness.setHand(player1, List.of(new RediscoverTheWay()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, plains, elves);
    }

    @Test
    @DisplayName("Chapter III triggers for each noncreature spell and can grant double strike to different creatures")
    void chapterIIITriggersMoreThanOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveChapterIII();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III does not trigger for an opponent's noncreature spell")
    void chapterIIIIgnoresOpponentSpells() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveChapterIII();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III's keyword grant and spell-cast trigger expire at the end of the turn")
    void chapterIIIExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveChapterIII();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void resolveChapterIII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RediscoverTheWay());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rediscover the Way");
    }

    private void addSagaMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}

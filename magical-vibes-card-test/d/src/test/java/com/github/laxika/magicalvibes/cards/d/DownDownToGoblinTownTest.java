package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({DownDownToGoblinTown.class, Forest.class, GrizzlyBears.class, Opt.class})
class DownDownToGoblinTownTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I reveals an opponent's hand and discards a chosen nonland card")
    void chapterIChoosesNonlandCardToDiscard() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        Card instant = new Opt();
        harness.setHand(player2, List.of(land, creature, instant));
        addSagaWithLore(0);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("Chapter II amasses Goblins and creates a Goblin Army when needed")
    void chapterIIAmassesGoblins() {
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army).isNotNull();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
    }

    @Test
    @DisplayName("Chapter II adds a counter and Goblin subtype to an existing Army")
    void chapterIIUsesExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Chapters III and IV drain an opponent for one life")
    void chaptersIIIAndIVDrainOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent saga = addSagaWithLore(2);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        saga.setCounterCount(CounterType.LORE, 3);
        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Chapter I resolves without a discard when the opponent has only lands")
    void chapterILeavesLandOnlyHandUnchanged() {
        Card land = new Forest();
        harness.setHand(player2, List.of(land));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter I resolves without a choice when the opponent's hand is empty")
    void chapterIHandlesEmptyHand() {
        harness.setHand(player2, List.of());
        addSagaWithLore(0);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter II ignores an opponent's Army and creates its controller's Goblin Army")
    void chapterIIIgnoresOpponentsArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army).isNotNull();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The Saga remains while chapter IV is on the stack and is sacrificed afterward")
    void chapterIVSacrificesSagaAfterResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent saga = addSagaWithLore(3);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DownDownToGoblinTown());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

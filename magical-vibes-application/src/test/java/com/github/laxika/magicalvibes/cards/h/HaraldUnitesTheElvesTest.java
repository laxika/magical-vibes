package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HaraldUnitesTheElvesTest extends BaseCardTest {

    @Test
    void chapterIMillsAndMayReturnAnElf() {
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card(), new Card()));
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void chapterIIPutsCountersOnEachElfYouControl() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent nonElf = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingElf = addCreatureReady(player2, new LlanowarElves());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIITargetsAnOpposingCreatureForEachElfThatAttacks() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void chapterIIIOnlyTriggersForElvesYouControl() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent opposingElf = addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opposingElf.getPowerModifier()).isZero();
        assertThat(opposingElf.getToughnessModifier()).isZero();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonLeviathan.class, CoralMerfolk.class, GrizzlyBears.class})
class SummonLeviathanTest extends BaseCardTest {

    @Test
    void chapterIReturnsNonSeaCreaturesButLeavesSeaCreatures() {
        Permanent saga = addSagaWithLore(0);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CoralMerfolk());

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIDrawsForEachAttackingSeaCreature() {
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card()));
        addSagaWithLore(1);
        addCreatureReady(player2, new CoralMerfolk());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIIITemporaryTriggerSurvivesSagaSacrifice() {
        harness.setLibrary(player1, List.of(new Card(), new Card()));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SummonLeviathan);

        addCreatureReady(player2, new CoralMerfolk());
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonLeviathan());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

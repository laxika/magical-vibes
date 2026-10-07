package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonLeviathan.class, CoralMerfolk.class, GrizzlyBears.class, Unsummon.class})
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
        harness.setLibrary(player1, List.of(new CoralMerfolk(), new CoralMerfolk(), new CoralMerfolk()));
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
        harness.setLibrary(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));
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

    @Test
    void castingSagaTriggersChapterIAndReturnsCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new CoralMerfolk());

        harness.castFromHand(player1, new SummonLeviathan(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertOnBattlefield(player1, "Summon: Leviathan");
        assertThat(findPermanent(player1, "Summon: Leviathan").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterIIDrawsSeparatelyForMultipleFriendlySeaCreaturesIncludingItself() {
        harness.setLibrary(player1, List.of(new CoralMerfolk(), new CoralMerfolk(), new CoralMerfolk()));
        Permanent saga = addSagaWithLore(1);
        saga.setSummoningSick(false);
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        resolveAllTriggers();
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIIITriggerExpiresBeforeNextTurn() {
        harness.setLibrary(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));
        addSagaWithLore(2);
        advanceToNextChapter();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player2, new CoralMerfolk());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent saga = addSagaWithLore(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, saga.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Unsummon");
        harness.assertOnBattlefield(player1, "Summon: Leviathan");
        harness.assertNotInHand(player1, "Summon: Leviathan");
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

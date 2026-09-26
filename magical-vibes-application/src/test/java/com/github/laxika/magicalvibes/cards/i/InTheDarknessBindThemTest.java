package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InTheDarknessBindThem.class, GrizzlyBears.class})
class InTheDarknessBindThemTest extends BaseCardTest {

    @Test
    void firstThreeChaptersCreateWraithsAndTemptTheRing() {
        Permanent saga = addSagaWithLore(0);

        for (int chapter = 1; chapter <= 3; chapter++) {
            advanceToNextChapter();

            List<Permanent> wraiths = findPermanents(player1, "Wraith");
            assertThat(wraiths).hasSize(chapter);
            Permanent latestWraith = wraiths.getLast();
            assertThat(gqs.getEffectivePower(gd, latestWraith)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, latestWraith)).isEqualTo(3);
            assertThat(gqs.effectiveCreatureSubtypes(gd, latestWraith)).contains(CardSubtype.WRAITH);
            assertThat(gqs.hasKeyword(gd, latestWraith, Keyword.MENACE)).isTrue();

            chooseRingBearer(latestWraith);
            resolveAllTriggers();
        }

        assertThat(gd.ringLevels.get(player1.getId())).isEqualTo(3);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    @Test
    void finalChapterStealsOneCreaturePerOpponentUntapsAndHastesThem() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        Permanent saga = addSagaWithLore(3);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponentCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opponentCreature.getId());
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(opponentCreature.getId())).isTrue();

        chooseRingBearer(opponentCreature);
        resolveAllTriggers();
        assertThat(gd.ringLevels.get(player1.getId())).isEqualTo(1);
        assertThat(saga).isNotIn(gd.playerBattlefields.get(player1.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(opponentCreature.getId());
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(opponentCreature.getId())).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InTheDarknessBindThem());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void chooseRingBearer(Permanent creature) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            assertThat(choice.validPermanentIds()).contains(creature.getId());
            harness.handlePermanentChosen(player1, creature.getId());
        }
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

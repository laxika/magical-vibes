package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWarGames.class, GrizzlyBears.class, ObsidianBattleAxe.class})
class TheWarGamesTest extends BaseCardTest {

    @Test
    void chapterICreatesTappedWarriorsAndGoadsThemWhileSagaRemains() {
        addSagaWithLore(0);

        advanceToNextChapter();

        List<Permanent> ownWarriors = warriors(player1);
        List<Permanent> opposingWarriors = warriors(player2);
        assertThat(ownWarriors).hasSize(3);
        assertThat(opposingWarriors).hasSize(3);
        assertThat(ownWarriors).allMatch(Permanent::isTapped);
        assertThat(opposingWarriors).allMatch(Permanent::isTapped);
        assertThat(ownWarriors).allMatch(warrior -> als.getMustAttackRequirementCount(gd, warrior) == 1);
        assertThat(opposingWarriors).allMatch(warrior -> als.getMustAttackRequirementCount(gd, warrior) == 1);
    }

    @Test
    void chaptersIIAndIIIPutCountersOnAllWarriors() {
        Permanent saga = addSagaWithLore(0);
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        List<Permanent> warriors = warriors(player1);
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();
        assertThat(warriors).allMatch(warrior -> warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
        assertThat(nonWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        assertThat(warriors).allMatch(warrior -> warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    void chapterIVExilesChosenCreatureAndAllWarriors() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        List<Permanent> warriors = warriors(player1);
        List<Permanent> opposingWarriors = warriors(player2);

        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(chosen.getId(), remaining.getId());

        harness.handlePermanentChosen(player1, chosen.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(warriors);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContainAnyElementsOf(opposingWarriors);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("The War Games"));
    }

    @Test
    void chaptersIIAndIIIDoNotPutCountersOnNoncreatureWarriors() {
        Permanent saga = addSagaWithLore(1);
        Permanent axe = harness.addToBattlefieldAndReturn(player2, new ObsidianBattleAxe());

        advanceToNextChapter();
        assertThat(axe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        assertThat(axe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIVCreatesASeparateTriggerBeforeExilingWarriors() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        List<Permanent> ownWarriors = warriors(player1);
        List<Permanent> opposingWarriors = warriors(player2);
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                    .validIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());
            harness.handlePermanentChosen(player1, chosen.getId());

            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chosen);
            assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(ownWarriors);
            assertThat(gd.playerBattlefields.get(player2.getId())).containsAll(opposingWarriors);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);

            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .doesNotContainAnyElementsOf(ownWarriors).contains(other);
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .doesNotContainAnyElementsOf(opposingWarriors);
        });
    }

    @Test
    void decliningChapterIVKeepsCreaturesAndEndsGoad() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        List<Permanent> ownWarriors = warriors(player1);
        List<Permanent> opposingWarriors = warriors(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsAll(ownWarriors).contains(creature).doesNotContain(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsAll(opposingWarriors);
        assertThat(ownWarriors).allMatch(warrior -> als.getMustAttackRequirementCount(gd, warrior) == 0);
        assertThat(opposingWarriors).allMatch(warrior -> als.getMustAttackRequirementCount(gd, warrior) == 0);
    }

    @Test
    void chapterIVCannotExileWarriorsWithoutANontokenCreature() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        List<Permanent> ownWarriors = warriors(player1);
        List<Permanent> opposingWarriors = warriors(player2);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(ownWarriors).doesNotContain(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsAll(opposingWarriors).contains(opposingCreature);
    }

    @Test
    void chapterIVExilesNoncreatureWarriorsAsWell() {
        addSagaWithLore(3);
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent axe = harness.addToBattlefieldAndReturn(player2, new ObsidianBattleAxe());

        advanceToNextChapter();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(axe);
    }

    @Test
    void chaptersIIAndIIIAlsoPutCountersOnOpposingWarriors() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        List<Permanent> opposingWarriors = warriors(player2);

        advanceToNextChapter();
        assertThat(opposingWarriors).hasSize(3)
                .allMatch(warrior -> warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        assertThat(opposingWarriors)
                .allMatch(warrior -> warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWarGames());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private List<Permanent> warriors(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .toList();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blink.class, GrizzlyBears.class})
class BlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I shuffles a creature and its owner investigates")
    void chapterIShufflesCreatureAndOwnerInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        addSagaWithLore(0);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Chapter II creates an Alien Angel that stops being a creature when an opponent casts a creature spell")
    void chapterIITokenBecomesNoncreatureUntilEndOfTurn() {
        addSagaWithLore(1);
        resolveNextChapter();

        Permanent alienAngel = findPermanent(player1, "Alien Angel");
        assertThat(gqs.isCreature(gd, alienAngel)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, alienAngel)).isFalse();
        assertThat(gqs.isArtifact(gd, alienAngel)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, alienAngel)).isTrue();
    }

    @Test
    @DisplayName("Chapter III also shuffles a target creature and investigates")
    void chapterIIIShufflesCreatureAndInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Blink());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveNextChapter() {
        triggerNextChapter();
        harness.passBothPriorities();
    }
}

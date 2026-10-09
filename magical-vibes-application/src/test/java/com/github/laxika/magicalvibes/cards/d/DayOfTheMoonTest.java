package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DayOfTheMoon.class, AirElemental.class, GrizzlyBears.class})
class DayOfTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Each chapter chooses a creature name and goads matching creatures")
    void chapterGoadsCreaturesWithChosenName() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DayOfTheMoon());
        saga.setCounterCount(CounterType.LORE, 0);
        Permanent matching = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonMatching = addCreatureReady(player2, new AirElemental());

        advanceToNextChapter();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Grizzly Bears").doesNotContain("Day of the Moon");
        harness.handleListChoice(player1, "Grizzly Bears");
        assertThat(saga.getChosenName()).isEqualTo("Grizzly Bears");

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(nonMatching))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatCode(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(matching),
                gd.playerBattlefields.get(player2.getId()).indexOf(nonMatching))))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("Later chapters goad creatures with previously chosen names")
    void laterChaptersRememberEarlierNames(int chapter) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DayOfTheMoon());
        saga.setCounterCount(CounterType.LORE, 0);
        Permanent originalBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        advanceToNextChapter();
        harness.handleListChoice(player1, "Grizzly Bears");
        originalBear.tap();
        for (int nextChapter = 2; nextChapter < chapter; nextChapter++) {
            advanceToNextChapter();
            harness.handleListChoice(player1, "Air Elemental");
        }
        Permanent newBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleListChoice(player1, "Air Elemental");

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(elemental))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(newBear);
        if (chapter == 3) {
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        }
    }

    @Test
    @DisplayName("A chapter still goads matching creatures if the Saga leaves before resolution")
    void chapterResolvesAfterSagaLeavesBattlefield() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DayOfTheMoon());
        saga.setCounterCount(CounterType.LORE, 0);
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleListChoice(player1, "Grizzly Bears"));

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creatures entering after a chapter resolves are not goaded by that chapter")
    void chapterOnlyGoadsCreaturesPresentAtResolution() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new DayOfTheMoon());
        saga.setCounterCount(CounterType.LORE, 0);
        Permanent original = addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleListChoice(player1, "Grizzly Bears");
        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(original))))
                .doesNotThrowAnyException();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}

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

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

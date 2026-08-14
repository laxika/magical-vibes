package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WardensOfTheCycleTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Morbid mode gains 2 life")
    void gainsLifeWithMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "You gain 2 life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Morbid draw mode draws a card and loses 1 life")
    void drawsAndLosesLifeWithMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        harness.handleListChoice(player1, "You draw a card and you lose 1 life.");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

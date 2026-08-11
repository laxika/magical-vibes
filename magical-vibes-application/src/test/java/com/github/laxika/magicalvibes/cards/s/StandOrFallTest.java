package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandOrFallTest extends BaseCardTest {

    private void advanceToControllerCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("separates the defending player's creatures at the beginning of combat")
    void separatesDefendingPlayersCreatures() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToControllerCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("allows only creatures in the chosen pile to block")
    void chosenPileIsTheOnlyBlockablePile() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getBlockLegalityService().canBlock(gd, spider)).isFalse();
    }

    @Test
    @DisplayName("the defending player may choose the other pile")
    void defendingPlayerMayChoosePileTwo() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(1);
        assertThat(harness.getBlockLegalityService().canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new StandOrFall());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }
}

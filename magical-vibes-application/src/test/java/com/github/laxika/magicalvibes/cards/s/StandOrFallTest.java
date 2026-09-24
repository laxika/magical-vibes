package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StandOrFall.class, RagingKavu.class, RazorfootGriffin.class})
class StandOrFallTest extends BaseCardTest {

    private void advanceToControllerCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("separates the defending player's creatures at the beginning of combat")
    void separatesDefendingPlayersCreatures() {
        harness.addToBattlefield(player1, new StandOrFall());
        addCreatureReady(player1, new RagingKavu());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());

        advanceToControllerCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(kavu.getId());
    }

    @Test
    @DisplayName("allows only creatures in the chosen pile to block")
    void chosenPileIsTheOnlyBlockablePile() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        Permanent griffin = addCreatureReady(player2, new RazorfootGriffin());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getBlockLegalityService().canBlock(gd, griffin)).isFalse();
    }

    @Test
    @DisplayName("the defending player may choose the other pile")
    void defendingPlayerMayChoosePileTwo() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        addCreatureReady(player2, new RazorfootGriffin());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(1);
        assertThat(harness.getBlockLegalityService().canBlock(gd, kavu)).isFalse();
    }

    @Test
    @DisplayName("an empty chosen pile leaves all creatures unable to block")
    void emptyChosenPileBarsAllCreatures() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        Permanent griffin = addCreatureReady(player2, new RazorfootGriffin());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .isEmpty();
        assertThat(harness.getBlockLegalityService().canBlock(gd, kavu)).isFalse();
        assertThat(harness.getBlockLegalityService().canBlock(gd, griffin)).isFalse();
    }

    @Test
    @DisplayName("does not let a creature that enters after the split block")
    void creatureEnteringAfterSplitCannotBlock() {
        harness.addToBattlefield(player1, new StandOrFall());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());

        advanceToControllerCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, true);

        Permanent laterCreature = addCreatureReady(player2, new RazorfootGriffin());

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getBlockLegalityService().canBlock(gd, laterCreature)).isFalse();
    }

    @Test
    @DisplayName("does not prompt when the defending player controls no creatures")
    void doesNotPromptWithoutCreatures() {
        harness.addToBattlefield(player1, new StandOrFall());

        advanceToControllerCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new StandOrFall());
        addCreatureReady(player2, new RagingKavu());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }
}

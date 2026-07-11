package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScatteringStrokeTest extends BaseCardTest {

    // Player1 casts Grizzly Bears (mana value 2); Player2 counters it with Scattering Stroke.
    private GrizzlyBears prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ScatteringStroke()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {2}{U}{U}

        return bears;
    }

    // Player2 (caster) wins the clash: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    // Advances into Player2's next precombat main phase so the delayed reward fires onto the stack.
    private void advanceToCasterNextMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
    }

    // ===== Won clash → counter + delayed mana equal to the countered spell's mana value =====

    @Test
    @DisplayName("Winning the clash counters the spell and lets the caster add {C} equal to its mana value next main phase")
    void wonClashSchedulesManaEqualToCounteredSpellManaValue() {
        GrizzlyBears bears = prepareCounterTarget();
        stackClashWinForCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        // Spell was countered.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Delayed reward registered for the caster, amount = Grizzly Bears' mana value (2).
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.controllerId()).isEqualTo(player2.getId());
        assertThat(reward.amount()).isEqualTo(2);
        assertThat(reward.color()).isEqualTo(ManaColor.COLORLESS);

        // At the caster's next main phase, they may add that much {C}.
        advanceToCasterNextMainPhase();
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();

        harness.passBothPriorities(); // resolve the delayed "you may" trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the delayed reward adds no mana")
    void wonClashDelayedRewardMayBeDeclined() {
        GrizzlyBears bears = prepareCounterTarget();
        stackClashWinForCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        advanceToCasterNextMainPhase();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    // ===== Lost clash → counter but no delayed mana reward =====

    @Test
    @DisplayName("Losing the clash counters the spell but schedules no mana")
    void lostClashCountersButSchedulesNoMana() {
        GrizzlyBears bears = prepareCounterTarget();
        // Player2 loses the clash: player1 reveals the higher mana value.
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }
}

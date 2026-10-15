package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoundReflection.class, FlameJavelin.class})
class WoundReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("At end step, opponent loses life equal to life lost this turn (damage counts)")
    void doublesLifeLostToDamage() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        // Flame Javelin deals 4 damage to the opponent; damage causes loss of life.
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        resolveEndStep(player1);

        // Wound Reflection: opponent loses another 4, the life they lost this turn.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Opponent who lost no life this turn loses nothing")
    void noLifeLostNoEffect() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts direct life loss rather than net life change")
    void countsDirectLifeLossAfterLifeGain() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 3, "test");
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2);
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Controller is unaffected even if the controller lost life this turn")
    void controllerNotAffected() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // The controller takes 4 damage this turn.
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);

        resolveEndStep(player1);

        // Only opponents lose life; the controller stays at 16, opponent untouched.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's turn")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);

        // On the opponent's own turn they lose life, then Wound Reflection still fires at end step.
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        resolveEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Life lost in response to the end-step trigger is counted at resolution")
    void countsDamageInResponseToTrigger() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Each Wound Reflection counts life lost to the earlier resolving trigger")
    void multipleReflectionsCompoundLifeLoss() {
        harness.addToBattlefield(player1, new WoundReflection());
        harness.addToBattlefield(player1, new WoundReflection());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts life lost before Wound Reflection entered the battlefield")
    void countsLifeLostBeforeEnteringBattlefield() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new WoundReflection());

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    /** Advances into the given player's end step and resolves the Wound Reflection trigger. */
    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}

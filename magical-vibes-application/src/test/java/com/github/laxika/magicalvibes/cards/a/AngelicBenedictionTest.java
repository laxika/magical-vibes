package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicBenedictionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone: exalted boosts the attacker and lets you tap target creature")
    void attacksAloneBoostsAndTaps() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone (index 1; enchantment is 0)

        // Both triggers (exalted boost + the optional tap) are on the stack; resolve until the tap
        // "you may" prompt surfaces (CR 603.5 resolution-time may ability).
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, enemy.getId());

        assertThat(enemy.isTapped()).isTrue();
        // Exalted +1/+1 on the lone attacker (still applied through the rest of the turn).
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the tap leaves the target untapped")
    void decliningLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(enemy.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking with more than one creature: no exalted boost and no tap trigger")
    void noTriggerWhenNotAlone() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent one = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2)); // two attackers — not alone

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(enemy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, one)).isEqualTo(2);
    }

    private void resolveUntilMayPrompt() {
        for (int i = 0; i < 4 && !(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}

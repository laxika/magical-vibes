package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicBenediction.class, CylianElf.class})
class AngelicBenedictionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone: exalted boosts the attacker and lets you tap target creature")
    void attacksAloneBoostsAndTaps() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent attacker = addCreatureReady(player1, new CylianElf());
        Permanent enemy = addCreatureReady(player2, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone (index 1; enchantment is 0)

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enemy.getId());
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(enemy.isTapped()).isTrue();
        // Exalted +1/+1 on the lone attacker (still applied through the rest of the turn).
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the tap leaves the target untapped")
    void decliningLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        addCreatureReady(player1, new CylianElf());
        Permanent enemy = addCreatureReady(player2, new CylianElf());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enemy.getId());
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(enemy.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking with more than one creature: no exalted boost and no tap trigger")
    void noTriggerWhenNotAlone() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent one = addCreatureReady(player1, new CylianElf());
        addCreatureReady(player1, new CylianElf());
        Permanent enemy = addCreatureReady(player2, new CylianElf());

        declareAttackers(player1, List.of(1, 2)); // two attackers — not alone

        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice).isFalse();
        assertThat(enemy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, one)).isEqualTo(2);
    }

    @Test
    @DisplayName("The tap trigger may target another creature you control")
    void canTapOwnCreature() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent attacker = addCreatureReady(player1, new CylianElf());
        Permanent ally = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ally.getId());
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(ally.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's lone attacker triggers neither ability")
    void opponentsAttackDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelicBenediction());
        Permanent attacker = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    private void resolveUntilMayPrompt() {
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}

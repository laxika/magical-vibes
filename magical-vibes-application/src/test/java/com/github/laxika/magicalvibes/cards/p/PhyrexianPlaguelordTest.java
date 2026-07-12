package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianPlaguelordTest extends BaseCardTest {

    // ===== Ability 0: {T}, Sacrifice this creature: target creature gets -4/-4 =====

    @Test
    @DisplayName("Tap/sacrifice ability gives target creature -4/-4 and sacrifices the Plaguelord")
    void tapSacAbilityGivesMinusFourMinusFour() {
        addPlaguelordReady(player1);
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(7);
        bigCreature.setToughness(7);
        harness.addToBattlefield(player2, bigCreature);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        // Plaguelord sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap/sacrifice ability kills a 4-or-less toughness creature")
    void tapSacAbilityKillsSmallCreature() {
        addPlaguelordReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tap/sacrifice ability cannot be activated while tapped")
    void tapSacAbilityCannotActivateWhenTapped() {
        Permanent plaguelord = addPlaguelordReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        plaguelord.tap();

        assertThat(gd.stack).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("-4/-4 wears off at end of turn")
    void minusFourWearsOffAtEndOfTurn() {
        addPlaguelordReady(player1);
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(8);
        bigCreature.setToughness(8);
        harness.addToBattlefield(player2, bigCreature);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(8);
    }

    // ===== Ability 1: Sacrifice a creature: target creature gets -1/-1 =====

    @Test
    @DisplayName("Sacrifice-a-creature ability gives target creature -1/-1 and sacrifices the fodder")
    void sacCreatureAbilityGivesMinusOneMinusOne() {
        addPlaguelordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(3);
        harness.addToBattlefield(player2, bigCreature);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        // Fodder sacrificed, Plaguelord survives
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability does not require tapping the Plaguelord")
    void sacCreatureAbilityDoesNotTap() {
        Permanent plaguelord = addPlaguelordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.handlePermanentChosen(player1, fodderId);

        assertThat(plaguelord.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helper =====

    private Permanent addPlaguelordReady(Player player) {
        PhyrexianPlaguelord card = new PhyrexianPlaguelord();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

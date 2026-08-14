package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendelhavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pendelhaven adds one green mana")
    void tapsForGreenMana() {
        Permanent pendelhaven = addPendelhavenReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pendelhaven.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a 1/1 creature +1/+2 until end of turn")
    void boostsOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addPendelhavenReady(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 1, null, elves.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is not 1/1")
    void cannotTargetNonOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Ability can target an opponent's 1/1 creature")
    void canTargetOpponentCreature() {
        addPendelhavenReady(player1);
        Permanent ghost = addCreatureReady(player2, new GhostWarden());

        harness.activateAbility(player1, 1, null, ghost.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ghost)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ghost)).isEqualTo(3);
    }

    private Permanent addPendelhavenReady(Player player) {
        Permanent pendelhaven = new Permanent(new Pendelhaven());
        pendelhaven.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(pendelhaven);
        return pendelhaven;
    }
}

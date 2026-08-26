package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrissNantukoPrimus.class, GrizzlyBears.class, Forest.class})
class ThrissNantukoPrimusTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives target creature +5/+5 until end of turn")
    void abilityBoostsTargetCreature() {
        addReadyThriss(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadyThriss(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityRequiresCreatureTarget() {
        addReadyThriss(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyThriss(Player player) {
        Permanent thriss = new Permanent(new ThrissNantukoPrimus());
        thriss.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(thriss);
        return thriss;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}

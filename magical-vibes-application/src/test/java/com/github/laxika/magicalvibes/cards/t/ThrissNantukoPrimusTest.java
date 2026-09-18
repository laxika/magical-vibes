package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrissNantukoPrimus.class, GiantWarthog.class, KrosanVerge.class})
class ThrissNantukoPrimusTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives target creature +5/+5 until end of turn")
    void abilityBoostsTargetCreature() {
        addCreatureReady(player1, new ThrissNantukoPrimus());
        Permanent target = addCreatureReady(player2, new GiantWarthog());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ThrissNantukoPrimus());
        Permanent target = addCreatureReady(player2, new GiantWarthog());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityRequiresCreatureTarget() {
        addCreatureReady(player1, new ThrissNantukoPrimus());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}

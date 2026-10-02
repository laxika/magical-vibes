package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlmightyBrushwagg.class})
class AlmightyBrushwaggTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3}{G} gives Almighty Brushwagg +3/+3 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(7);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyBrushwagg();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Brushwagg can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        brushwagg.setSummoningSick(true);
        brushwagg.setTapped(true);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(4);
        assertThat(brushwagg.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Four generic mana cannot pay the green part of the activation cost")
    void cannotActivateWithoutGreenMana() {
        addReadyBrushwagg();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boosting one Brushwagg does not boost another")
    void boostsOnlyTheSource() {
        Permanent source = addReadyBrushwagg();
        Permanent other = addReadyBrushwagg();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("A boosted Brushwagg tramples over a blocking Brushwagg")
    void boostedCreatureDealsExcessCombatDamageToPlayer() {
        Permanent attacker = addReadyBrushwagg();
        Permanent blocker = addCreatureReady(player2, new AlmightyBrushwagg());
        addMana(player1, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    private Permanent addReadyBrushwagg() {
        return addCreatureReady(player1, new AlmightyBrushwagg());
    }

    private void addMana(Player player, int amount) {
        harness.addMana(player, ManaColor.GREEN, amount);
        harness.addMana(player, ManaColor.COLORLESS, amount * 3);
    }
}

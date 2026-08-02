package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayShadeTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: Gateway Shade gets +1/+1 until end of turn")
    void blackManaAbilityBoostsShade() {
        addShade();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = findPermanent(player1, "Gateway Shade");
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Tapping an untapped Gate gives Gateway Shade +2/+2")
    void tappingGateBoostsShadeAndTapsGate() {
        addShade();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent shade = findPermanent(player1, "Gateway Shade");
        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
        assertThat(gate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Gate ability cannot be activated without an untapped Gate")
    void gateAbilityRequiresUntappedGate() {
        addShade();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gateway Shade's boosts wear off at cleanup")
    void boostsWearOffAtCleanup() {
        addShade();
        harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent shade = findPermanent(player1, "Gateway Shade");
        assertThat(shade.getPowerModifier()).isZero();
        assertThat(shade.getToughnessModifier()).isZero();
    }

    private void addShade() {
        harness.addToBattlefield(player1, new GatewayShade());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PlatedRootwalla.class)
class PlatedRootwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +3/+3 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent rootwalla = addCreatureReady(player1, new PlatedRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, rootwalla)).isEqualTo(6);
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addCreatureReady(player1, new PlatedRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addCreatureReady(player1, new PlatedRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent rootwalla = addCreatureReady(player1, new PlatedRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pump ability can be activated while the creature has summoning sickness")
    void pumpAbilityIgnoresSummoningSickness() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new PlatedRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(6);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(6);
    }
}

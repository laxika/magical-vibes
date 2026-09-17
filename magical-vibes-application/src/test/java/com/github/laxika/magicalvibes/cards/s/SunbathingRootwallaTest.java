package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunbathingRootwalla.class, Forest.class, Island.class, Mountain.class})
class SunbathingRootwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Domain pump scales with distinct basic land types")
    void domainPumpScalesWithBasicLandTypes() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(5);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Domain counts each basic land type only once")
    void domainCountsDistinctTypesOnly() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The domain pump can be activated only once each turn")
    void pumpAbilityOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new SunbathingRootwalla());
        addActivationMana(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The domain pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(2);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(2);
    }

    private void addActivationMana() {
        addActivationMana(1);
    }

    private void addActivationMana(int activations) {
        harness.addMana(player1, ManaColor.GREEN, activations);
        harness.addMana(player1, ManaColor.COLORLESS, activations * 3);
    }
}

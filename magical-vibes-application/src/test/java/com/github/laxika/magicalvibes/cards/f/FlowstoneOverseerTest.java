package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KillSwitch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneOverseer.class, FlowstoneCrusher.class, KillSwitch.class})
class FlowstoneOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/-1 until end of turn")
    void boostsTargetCreature() {
        Permanent target = setupBattlefield();
        Permanent overseer = findPermanent(player1, "Flowstone Overseer");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(overseer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOff() {
        Permanent target = setupBattlefield();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        setupBattlefield();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KillSwitch());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two red mana")
    void requiresTwoRedMana() {
        Permanent target = setupBattlefield(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        setupBattlefield();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlowstoneCrusher());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Can target itself while tapped and summoning sick")
    void canTargetItselfWhileTappedAndSummoningSick() {
        setupBattlefield();
        Permanent overseer = findPermanent(player1, "Flowstone Overseer");
        overseer.tap();
        overseer.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, overseer.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, overseer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, overseer)).isEqualTo(3);
        assertThat(overseer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations accumulate and put a zero-toughness creature into its graveyard")
    void repeatedActivationsCanKillTarget() {
        Permanent target = setupBattlefield(8);

        for (int i = 1; i <= 3; i++) {
            harness.activateAbility(player1, 0, null, target.getId());
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4 + i);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4 - i);
        }

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flowstone Crusher");
        harness.assertInGraveyard(player2, "Flowstone Crusher");
        harness.assertOnBattlefield(player1, "Flowstone Overseer");
    }

    @Test
    @DisplayName("An activated ability still resolves after the Overseer dies")
    void abilityResolvesAfterSourceDies() {
        Permanent target = setupBattlefield(10);
        Permanent overseer = findPermanent(player1, "Flowstone Overseer");

        harness.activateAbility(player1, 0, null, target.getId());
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, overseer.getId());
        }
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Flowstone Overseer");
        harness.assertInGraveyard(player1, "Flowstone Overseer");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    private Permanent setupBattlefield() {
        return setupBattlefield(2);
    }

    private Permanent setupBattlefield(int redMana) {
        harness.addToBattlefield(player1, new FlowstoneOverseer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return target;
    }
}

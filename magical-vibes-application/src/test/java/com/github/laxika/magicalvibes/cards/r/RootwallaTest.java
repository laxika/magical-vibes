package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Rootwalla.class)
class RootwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rootwalla puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Rootwalla()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The ability requires green mana")
    void abilityRequiresGreenMana() {
        addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated once each turn")
    void canActivateOnceEachTurn() {
        Permanent rootwalla = addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated while Rootwalla is tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent rootwalla = addCreatureReady(player1, new Rootwalla());
        rootwalla.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.isTapped()).isTrue();
        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Second activation in same turn is rejected")
    void secondActivationInSameTurnIsRejected() {
        addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Activation limit applies before the first ability resolves")
    void secondActivationBeforeResolutionIsRejected() {
        Permanent rootwalla = addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(rootwalla.getEffectivePower()).isEqualTo(2);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Rootwalla has its own activation limit and boosts only itself")
    void separateCopiesHaveIndependentActivationLimits() {
        Permanent first = addCreatureReady(player1, new Rootwalla());
        Permanent second = addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activating the ability")
    void abilityCanBeActivatedWithSummoningSickness() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new Rootwalla());
        rootwalla.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent rootwalla = addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(4);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(rootwalla.getEffectivePower()).isEqualTo(2);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(2);
    }
}

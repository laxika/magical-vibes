package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BelbesArmor;
import com.github.laxika.magicalvibes.cards.c.ComplexAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Woodripper.class, BelbesArmor.class, ComplexAutomaton.class})
class WoodripperTest extends BaseCardTest {

    @Test
    @DisplayName("Woodripper enters with three fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new Woodripper(), "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent woodripper = findPermanent(player1, "Woodripper");
        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent woodripper = addReadyWoodripper(player1, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("Fading removes the last fade counter without sacrificing Woodripper")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent woodripper = addReadyWoodripper(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent woodripper = addReadyWoodripper(player1, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("Fading sacrifices Woodripper when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addReadyWoodripper(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("The activated ability removes a fade counter and destroys target artifact")
    void destroysTargetArtifact() {
        Permanent woodripper = addReadyWoodripper(player1, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BelbesArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
        harness.assertNotOnBattlefield(player2, "Belbe's Armor");
        harness.assertInGraveyard(player2, "Belbe's Armor");
    }

    @Test
    @DisplayName("The activated ability can destroy an artifact creature")
    void destroysTargetArtifactCreature() {
        Permanent woodripper = addReadyWoodripper(player1, 1);
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new ComplexAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
        harness.assertNotOnBattlefield(player2, "Complex Automaton");
        harness.assertInGraveyard(player2, "Complex Automaton");
    }

    @Test
    @DisplayName("The activated ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent woodripper = addReadyWoodripper(player1, 1);
        Permanent creature = addReadyWoodripper(player2, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability cannot be paid without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent woodripper = addReadyWoodripper(player1, 0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BelbesArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
    }

    private Permanent addReadyWoodripper(com.github.laxika.magicalvibes.model.Player player, int fadeCounters) {
        Permanent woodripper = addCreatureReady(player, new Woodripper());
        woodripper.setCounterCount(CounterType.FADE, fadeCounters);
        return woodripper;
    }
}

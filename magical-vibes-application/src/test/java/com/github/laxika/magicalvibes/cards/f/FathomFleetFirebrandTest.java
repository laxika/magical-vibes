package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FathomFleetFirebrand.class})
class FathomFleetFirebrandTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Fathom Fleet Firebrand puts it on the stack")
    void castingPutsOnStack() {
        FathomFleetFirebrand card = new FathomFleetFirebrand();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Activating ability puts the source ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent firebrand = addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(firebrand.getCard());
        assertThat(entry.getTargetId()).isEqualTo(firebrand.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Fathom Fleet Firebrand")
    void resolvingAbilityBoostsPower() {
        Permanent firebrand = addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(3);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
        assertThat(firebrand.getPowerModifier()).isEqualTo(1);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent firebrand = addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(5);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
        assertThat(firebrand.getPowerModifier()).isEqualTo(3);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent firebrand = addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(4);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firebrand.getPowerModifier()).isEqualTo(0);
        assertThat(firebrand.getToughnessModifier()).isEqualTo(0);
        assertThat(firebrand.getEffectivePower()).isEqualTo(2);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new FathomFleetFirebrand());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only 1 red mana (needs {1}{R})")
    void cannotActivateWithOnlyOneRedMana() {
        addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Generic mana cannot replace the required red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Firebrand can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent firebrand = harness.addToBattlefieldAndReturn(player1, new FathomFleetFirebrand());
        firebrand.setSummoningSick(true);
        firebrand.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(3);
        assertThat(firebrand.getEffectiveToughness()).isEqualTo(2);
        assertThat(firebrand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost applies only to the Firebrand whose ability was activated")
    void boostsOnlySourceAmongMultipleCopies() {
        Permanent source = addCreatureReady(player1, new FathomFleetFirebrand());
        Permanent other = addCreatureReady(player1, new FathomFleetFirebrand());
        Permanent opponent = addCreatureReady(player2, new FathomFleetFirebrand());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }
}

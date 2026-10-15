package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aetherling.class})
class AetherlingTest extends BaseCardTest {

    @Test
    @DisplayName("First ability exiles Aetherling")
    void firstAbilityExiles() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aetherling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aetherling"));
    }

    @Test
    @DisplayName("Exiled Aetherling returns at the beginning of the next end step")
    void returnsAtEndStep() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Aetherling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Aetherling"));
    }

    @Test
    @DisplayName("Second ability makes Aetherling unblockable this turn")
    void secondAbilityMakesUnblockable() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(aetherling.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOff() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aetherling.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Third ability gives +1/-1 until end of turn")
    void thirdAbilityBoosts() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fourth ability gives -1/+1 until end of turn")
    void fourthAbilityBoosts() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(6);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOff() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exiled Aetherling returns under its owner's control")
    void stolenAetherlingReturnsToOwner() {
        Aetherling card = new Aetherling();
        card.setOwnerId(player2.getId());
        Permanent aetherling = addCreatureReady(player1, card);
        gd.stolenCreatures.put(aetherling.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Aetherling");
        harness.assertOnBattlefield(player2, "Aetherling");
    }

    @Test
    @DisplayName("The delayed return uses the stack before Aetherling returns")
    void returnWaitsForDelayedTriggerResolution() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Aetherling");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aetherling");
    }

    @Test
    @DisplayName("Exiling during the end step waits until the following turn's end step")
    void exileDuringEndStepWaitsForNextEndStep() {
        addCreatureReady(player1, new Aetherling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aetherling");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Aetherling");
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Aetherling");
    }

    @Test
    @DisplayName("Repeated pumps can reduce Aetherling to zero toughness")
    void repeatedPumpsKillAtZeroToughness() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Aetherling");
        harness.assertInGraveyard(player1, "Aetherling");
    }

    @Test
    @DisplayName("The fourth ability's toughness boost expires at end of turn")
    void toughnessBoostWearsOff() {
        Permanent aetherling = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flickering clears pumps and unblockability and creates a new permanent")
    void flickerClearsTemporaryEffects() {
        Permanent original = addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Aetherling");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isCantBeBlocked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blue abilities cannot be paid for with colorless mana")
    void blueAbilitiesRequireBlueMana() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Aetherling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Aetherling can activate abilities while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent aetherling = harness.addToBattlefieldAndReturn(player1, new Aetherling());
        aetherling.setSummoningSick(true);
        aetherling.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(6);
    }

    @Test
    @DisplayName("A pump does nothing if Aetherling is exiled in response")
    void pumpDoesNotAffectAnExiledSource() {
        addCreatureReady(player1, new Aetherling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Aetherling");
        harness.passBothPriorities();
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Aetherling");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireAristocrat.class, RuneclawBear.class})
class VampireAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent vampPerm = addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Runeclaw Bear should be sacrificed
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");

        // Vampire Aristocrat should still be on the battlefield
        harness.assertOnBattlefield(player1, "Vampire Aristocrat");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(entry.getTargetId()).isEqualTo(vampPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Vampire Aristocrat +2/+2")
    void resolvingAbilityBoostsVampire() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(vamp.getPowerModifier()).isEqualTo(2);
        assertThat(vamp.getToughnessModifier()).isEqualTo(2);
        assertThat(vamp.getEffectivePower()).isEqualTo(4);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());

        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        UUID secondBearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondBearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(vamp.getPowerModifier()).isEqualTo(4);
        assertThat(vamp.getToughnessModifier()).isEqualTo(4);
        assertThat(vamp.getEffectivePower()).isEqualTo(6);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can sacrifice Vampire Aristocrat to its own ability")
    void canSacrificeItself() {
        addVampireAristocratReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Vampire should be sacrificed
        harness.assertNotOnBattlefield(player1, "Vampire Aristocrat");
        harness.assertInGraveyard(player1, "Vampire Aristocrat");

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vampire Aristocrat");
    }

    @Test
    @DisplayName("Ability resolves without a boost when Vampire sacrifices itself")
    void abilityResolvesWithoutBoostWhenVampireSacrificesItself() {
        addVampireAristocratReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // The non-targeting ability resolves but its source is no longer on the battlefield.
        harness.assertNotOnBattlefield(player1, "Vampire Aristocrat");
        harness.assertInGraveyard(player1, "Vampire Aristocrat");
    }

    @Test
    @DisplayName("Ability has no mana cost and can activate without mana")
    void canActivateWithoutMana() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Vampire Aristocrat")
    void activatingAbilityDoesNotTap() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getEffectivePower()).isEqualTo(4);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(4);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vamp.getPowerModifier()).isEqualTo(0);
        assertThat(vamp.getToughnessModifier()).isEqualTo(0);
        assertThat(vamp.getEffectivePower()).isEqualTo(2);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Auto-sacrifices when only one creature available")
    void autoSacrificesWhenOnlyOneCreatureAvailable() {
        addVampireAristocratReady(player1);
        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        // Vampire auto-sacrificed (only creature on battlefield)
        harness.assertNotOnBattlefield(player1, "Vampire Aristocrat");
        harness.assertInGraveyard(player1, "Vampire Aristocrat");
        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick, tapped Vampire Aristocrat can activate its ability")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent vamp = harness.addToBattlefieldAndReturn(player1, new VampireAristocrat());
        vamp.setSummoningSick(true);
        vamp.tap();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(vamp.isTapped()).isTrue();
        assertThat(vamp.getEffectivePower()).isEqualTo(4);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost applies only to the Vampire Aristocrat that activated")
    void boostAppliesOnlyToSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VampireAristocrat());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new VampireAristocrat());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.getEffectiveToughness()).isEqualTo(4);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addVampireAristocratReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VampireAristocrat());
        perm.setSummoningSick(false);
        return perm;
    }
}

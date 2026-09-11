package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HorseshoeCrab.class})
class HorseshoeCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Horseshoe Crab puts it on the stack")
    void castingPutsOnStack() {
        HorseshoeCrab crab = new HorseshoeCrab();
        harness.castFromHand(player1, crab, "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(crab);
    }

    @Test
    @DisplayName("Resolving puts Horseshoe Crab onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new HorseshoeCrab(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new HorseshoeCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Activating ability puts UntapSelf on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent crab = addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(crab.getId());
    }

    @Test
    @DisplayName("Resolving ability untaps Horseshoe Crab")
    void resolvingAbilityUntapsSelf() {
        Permanent crabPerm = addCreatureReady(player1, new HorseshoeCrab());
        crabPerm.tap();
        assertThat(crabPerm.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(crabPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when already untapped")
    void canActivateWhenAlreadyUntapped() {
        addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent crab = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent crabPerm = addCreatureReady(player1, new HorseshoeCrab());
        crabPerm.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crabPerm.isTapped()).isFalse();

        // Tap it again manually
        crabPerm.tap();
        assertThat(crabPerm.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crabPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent crab = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability while summoning sick because it has no tap cost")
    void canActivateWhileSummoningSick() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crab.isSummoningSick()).isTrue();
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new HorseshoeCrab());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability has no effect if Horseshoe Crab is removed before resolution")
    void abilityDoesNothingIfSourceRemoved() {
        Permanent crab = addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Horseshoe Crab before resolution
        gd.playerBattlefields.get(player1.getId()).remove(crab);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new HorseshoeCrab());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("activates ")).isTrue();
    }

    @Test
    @DisplayName("Resolving ability logs the untap")
    void resolvingAbilityLogsUntap() {
        Permanent crabPerm = addCreatureReady(player1, new HorseshoeCrab());
        crabPerm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains(" untaps.")).isTrue();
    }

    @Test
    @DisplayName("Unblocked Horseshoe Crab deals combat damage to defending player")
    void dealsCombatDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new HorseshoeCrab());
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}


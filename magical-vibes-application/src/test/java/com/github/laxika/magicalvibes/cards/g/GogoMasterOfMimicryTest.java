package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LithoformEngine;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RingsOfBrighthearth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GogoMasterOfMimicry.class, ProdigalPyromancer.class, RingsOfBrighthearth.class,
        Shock.class, TrialOfZeal.class, LithoformEngine.class})
class GogoMasterOfMimicryTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability X times")
    void copiesTriggeredAbilityXTimes() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerCardId = abilityCardIdOnStack(StackEntryType.TRIGGERED_ABILITY);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 2, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Copies an activated ability X times")
    void copiesActivatedAbilityXTimes() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GogoMasterOfMimicry());
        addCreatureReady(player1, new ProdigalPyromancer());

        int pyromancerIndex = findBattlefieldIndex(player1, "Prodigal Pyromancer");
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        UUID abilityCardId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 2, abilityCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not allow X to be zero")
    void xCannotBeZero() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, findBattlefieldIndex(player1, "Prodigal Pyromancer"), null, player2.getId());
        UUID abilityCardId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 0, abilityCardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    @DisplayName("Cannot be copied")
    void abilityCannotBeCopied() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RingsOfBrighthearth());
        addCreatureReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        UUID triggerCardId = abilityCardIdOnStack(StackEntryType.TRIGGERED_ABILITY);
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 1, triggerCardId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        UUID spellCardId = gd.stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, findBattlefieldIndex(player1, "Gogo, Master of Mimicry"), 1, spellCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each copy may choose its own new target")
    void copiesMayChooseDifferentTargets() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID originalId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, originalId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent's activated ability")
    void cannotTargetOpponentsAbility() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, 0, null, player1.getId());
        UUID originalId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, originalId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both X symbols must be paid")
    void cannotPayForTwoCopiesWithOnlyThreeMana() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID originalId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, originalId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Copies still resolve after the original ability's source dies")
    void copiesResolveAfterSourceDies() {
        addCreatureReady(player1, new GogoMasterOfMimicry());
        var pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID originalId = abilityCardIdOnStack(StackEntryType.ACTIVATED_ABILITY);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pyromancer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, originalId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({GogoMasterOfMimicry.class, LithoformEngine.class})
    @DisplayName("Gogo's creature spell can be copied")
    void creatureSpellCanBeCopied() {
        harness.addToBattlefield(player1, new LithoformEngine());
        GogoMasterOfMimicry gogo = new GogoMasterOfMimicry();
        harness.setHand(player1, List.of(gogo));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, 2, null, gogo.getId());
        harness.passBothPriorities();

        assertThat(gd.stack)
                .filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .hasSize(2);
    }

    private int findBattlefieldIndex(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, name));
    }

    private UUID abilityCardIdOnStack(StackEntryType type) {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == type)
                .findFirst()
                .orElseThrow()
                .getTargetableId();
    }
}

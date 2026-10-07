package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphereOfSafety.class, GoblinElectromancer.class, EtherealArmor.class, JaceArchitectOfThought.class})
class SphereOfSafetyTest extends BaseCardTest {

    @Test
    @DisplayName("A lone Sphere taxes {1} per attacker — it counts itself")
    void loneSphereTaxesOne() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addCreatureReady(player2, new GoblinElectromancer());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));

        // Combat auto-resolves (no blockers), so only the paid tax is observable here.
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking without paying the tax is illegal")
    void cannotAttackWithoutPaying() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addCreatureReady(player2, new GoblinElectromancer());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Two Spheres tax {2} each — {4} total per attacker")
    void taxScalesWithEnchantmentCount() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.addToBattlefield(player1, new SphereOfSafety());
        addCreatureReady(player2, new GoblinElectromancer());

        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));

        // Combat auto-resolves (no blockers), so only the paid tax is observable here.
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The tax is charged per attacking creature")
    void taxIsPerAttacker() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addCreatureReady(player2, new GoblinElectromancer());
        addCreatureReady(player2, new GoblinElectromancer());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Only the defending player's enchantments count")
    void countsOnlyDefendersEnchantments() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.addToBattlefield(player2, new SphereOfSafety());
        addCreatureReady(player2, new GoblinElectromancer());

        // player2's own Sphere doesn't raise the tax on player2's attackers.
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        // The creature sits at index 1 — player2's own Sphere occupies index 0.
        declareAttackers(player2, List.of(1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sphere also taxes attacks against its controller's planeswalker")
    void cannotAttackPlaneswalkerWithoutPaying() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        jace.setCounterCount(CounterType.LOYALTY, 4);
        addCreatureReady(player2, new GoblinElectromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, jace.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Paying the tax permits attacking a planeswalker")
    void canAttackPlaneswalkerAfterPaying() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        jace.setCounterCount(CounterType.LOYALTY, 4);
        addCreatureReady(player2, new GoblinElectromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, jace.getId())));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A controlled Aura counts even when attached to an opponent's creature")
    void countsAuraControlledByDefender() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        Permanent attacker = addCreatureReady(player2, new GoblinElectromancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        aura.setAttachedTo(attacker.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enchantment cards in hand and graveyard do not increase the tax")
    void ignoresEnchantmentsOutsideBattlefield() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.setHand(player1, List.of(new SphereOfSafety()));
        harness.setGraveyard(player1, List.of(new SphereOfSafety()));
        addCreatureReady(player2, new GoblinElectromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The attacker may decline to attack even when able to pay")
    void mayDeclineAttackWithManaAvailable() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        Permanent creature = addCreatureReady(player2, new GoblinElectromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of()));

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }
}

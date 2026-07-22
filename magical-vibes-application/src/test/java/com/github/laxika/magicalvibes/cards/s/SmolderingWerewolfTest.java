package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmolderingWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 1 damage to each of two target creatures")
    void etbDamagesTwoCreatures() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSmolderingWerewolf(List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB

        assertThat(creature1.getMarkedDamage()).isEqualTo(1);
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can target one creature")
    void etbCanTargetOneCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSmolderingWerewolf(List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can choose no targets")
    void etbCanChooseNoTargets() {
        castSmolderingWerewolf(List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Smoldering Werewolf");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> castSmolderingWerewolf(List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("{4}{R}{R} transforms into Erupting Dreadwolf")
    void transformAbilityFlipsToDreadwolf() {
        Permanent werewolf = addCreatureReady(player1, new SmolderingWerewolf());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isTrue();
        assertThat(werewolf.getCard().getName()).isEqualTo("Erupting Dreadwolf");
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Erupting Dreadwolf deals 2 damage to any target when it attacks")
    void backFaceAttackDealsDamageToAnyTarget() {
        Permanent werewolf = addCreatureReady(player1, new SmolderingWerewolf());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Erupting Dreadwolf can deal its attack damage to a player")
    void backFaceAttackDamagesPlayer() {
        harness.setLife(player2, 20);
        Permanent werewolf = addCreatureReady(player1, new SmolderingWerewolf());
        addCreatureReady(player2, new GrizzlyBears()); // blocker so combat pauses
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castSmolderingWerewolf(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SmolderingWerewolf()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}

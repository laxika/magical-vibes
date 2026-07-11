package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeteransArmamentsTest extends BaseCardTest {

    // ===== Granted trigger: "Whenever this creature attacks or blocks, it gets +1/+1
    //       until end of turn for each attacking creature." =====

    @Test
    @DisplayName("Equipped creature attacking alone gets +1/+1 (one attacking creature)")
    void attackingAloneGetsPlusOne() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent armaments = addReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost scales with the number of attacking creatures")
    void boostScalesWithAttackers() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent armaments = addReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());
        addReady(player1, new GrizzlyBears());
        addReady(player1, new GrizzlyBears());

        // Indices 0 (equipped), 2 and 3 are creatures (index 1 is the Equipment).
        declareAttackers(player1, List.of(0, 2, 3));
        harness.passBothPriorities();

        // Three attacking creatures → +3/+3.
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent armaments = addReady(player1, new VeteransArmaments());
        armaments.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Equipped creature that blocks is boosted per attacking creature")
    void blockingCreatureGetsBoost() {
        // Player 1 attacks with two creatures.
        Permanent attacker1 = addReady(player1, new GrizzlyBears());
        Permanent attacker2 = addReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        // Player 2's equipped creature blocks.
        Permanent blocker = addReady(player2, new GrizzlyBears());
        Permanent armaments = addReady(player2, new VeteransArmaments());
        armaments.setAttachedTo(blocker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Two attacking creatures → +2/+2 on the blocker.
        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("No trigger when the Equipment is not attached to the attacker")
    void noTriggerWhenUnattached() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        addReady(player1, new VeteransArmaments()); // present but unattached

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Veteran's Armaments"));
        assertThat(creature.getPowerModifier()).isEqualTo(0);
    }

    // ===== Trigger: "Whenever a Soldier creature enters, you may attach this Equipment to it." =====

    @Test
    @DisplayName("Accepting the may attaches the Equipment to the Soldier that entered")
    void attachesToEnteringSoldierOnAccept() {
        Permanent armaments = addReady(player1, new VeteransArmaments());

        harness.setHand(player1, List.of(new VeteranArmorsmith()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent soldier = soldierOnBattlefield(player1);
        assertThat(armaments.getAttachedTo()).isEqualTo(soldier.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Soldier creature entering")
    void doesNotTriggerForNonSoldier() {
        addReady(player1, new VeteransArmaments());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Equip {2} =====

    @Test
    @DisplayName("Resolving equip attaches the Equipment to the target creature")
    void resolvingEquipAttaches() {
        Permanent armaments = addReady(player1, new VeteransArmaments());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armaments.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent soldierOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Veteran Armorsmith"))
                .findFirst().orElseThrow();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}

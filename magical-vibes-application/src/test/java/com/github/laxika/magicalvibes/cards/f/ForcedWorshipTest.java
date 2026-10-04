package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForcedWorship.class, GrizzlyBears.class})
class ForcedWorshipTest extends BaseCardTest {

    @Test
    @DisplayName("Creature enchanted with Forced Worship cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new ForcedWorship());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creature enchanted with Forced Worship can still block")
    void enchantedCreatureCanBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ForcedWorship());
        auraPerm.setAttachedTo(blockerPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creature can attack again after Forced Worship is removed")
    void creatureCanAttackAfterRemoval() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new ForcedWorship());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.playerBattlefields.get(player2.getId()).remove(auraPerm);

        harness.beginAttackerDeclarationInput();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Activated ability returns Forced Worship to owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new ForcedWorship()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        int auraIndex = -1;
        var battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Forced Worship")) {
                auraIndex = i;
                break;
            }
        }
        assertThat(auraIndex).isGreaterThanOrEqualTo(0);
        assertThat(battlefield.get(auraIndex).getAttachedTo()).isEqualTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forced Worship");
        harness.assertInHand(player1, "Forced Worship");
    }

    @Test
    @DisplayName("Casting Forced Worship and resolving attaches it to target creature")
    void castingAndResolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new ForcedWorship()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forced Worship")
                        && bearsPerm.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Returning the Aura leaves the enchanted creature on the battlefield")
    void returningAuraLeavesCreatureInPlace() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ForcedWorship());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Forced Worship");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forced Worship");
        harness.assertNotOnBattlefield(player1, "Forced Worship");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("A stolen Forced Worship returns to its owner rather than its controller")
    void returnsToOwnerWhenControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ForcedWorship card = new ForcedWorship();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(aura.getId(), player1.getId());
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forced Worship");
        harness.assertNotInHand(player2, "Forced Worship");
        harness.assertNotOnBattlefield(player2, "Forced Worship");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Return ability can be activated during the opponent's end step")
    void canReturnDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ForcedWorship());
        aura.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forced Worship");
        harness.assertNotOnBattlefield(player1, "Forced Worship");
    }
}

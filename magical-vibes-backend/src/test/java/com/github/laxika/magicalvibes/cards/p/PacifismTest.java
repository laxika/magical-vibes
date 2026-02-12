package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PacifismTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Pacifism has correct card properties")
    void hasCorrectProperties() {
        Pacifism card = new Pacifism();

        assertThat(card.getName()).isEqualTo("Pacifism");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(EnchantedCreatureCantAttackOrBlockEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Pacifism puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, "W", 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Resolving Pacifism attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, "W", 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pacifism")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Cannot cast Pacifism without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Prevents attacking =====

    @Test
    @DisplayName("Creature enchanted with Pacifism cannot be declared as attacker")
    void enchantedCreatureCannotAttack() {
        // Player1 has a creature
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Attach Pacifism directly to the creature
        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifismPerm);

        // Try to attack with the pacified creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Pacified creature is excluded from attackable creature indices")
    void pacifiedCreatureNotInAttackableIndices() {
        // Player1 has two creatures: one pacified, one free
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent freeBears = new Permanent(new GrizzlyBears());
        freeBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(freeBears);

        // Attach Pacifism directly to the first creature
        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifismPerm);

        // Only the second creature can attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        // Pacified creature (index 0) cannot attack, so index 0 in attackable list maps to freeBears (index 1)
        // Attempting to attack with the pacified creature should fail
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        // Attacking with index 1 (the free creature) should succeed
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(bearsPerm.isAttacking()).isFalse();
    }

    // ===== Prevents blocking =====

    @Test
    @DisplayName("Creature enchanted with Pacifism cannot be declared as blocker")
    void enchantedCreatureCannotBlock() {
        // Player2 has a creature with Pacifism on it
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Attach Pacifism directly to the creature
        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(pacifismPerm);

        // Player1 has an attacking creature (index 1, after Pacifism at index 0)
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Pacified creature is excluded from blockable creature indices")
    void pacifiedCreatureNotInBlockableIndices() {
        // Player2 has two creatures: one pacified, one free
        Permanent pacifiedPerm = new Permanent(new GrizzlyBears());
        pacifiedPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pacifiedPerm);

        Permanent freePerm = new Permanent(new GrizzlyBears());
        freePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(freePerm);

        // Attach Pacifism directly to the first creature
        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(pacifiedPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(pacifismPerm);

        // Player1 has an attacking creature (added after Pacifism, so it's at index 1)
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // Only the free creature (index 1) can block; attacker is at index 1 on player1's battlefield
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));

        assertThat(freePerm.isBlocking()).isTrue();
        assertThat(pacifiedPerm.isBlocking()).isFalse();
    }

    // ===== Pacifism removed restores ability =====

    @Test
    @DisplayName("Creature can attack again after Pacifism is removed")
    void creatureCanAttackAfterPacifismRemoved() {
        // Player1 has a creature
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Attach Pacifism to it
        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifismPerm);

        // Verify creature can't attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        // Remove Pacifism from battlefield
        gd.playerBattlefields.get(player2.getId()).remove(pacifismPerm);

        // Now creature can attack — declareAttackers should not throw
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));

        // The game auto-advances through combat, but the call succeeding proves the creature could attack
    }

    @Test
    @DisplayName("Creature can block again after Pacifism is removed")
    void creatureCanBlockAfterPacifismRemoved() {
        // Player2 has a creature with Pacifism
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(pacifismPerm);

        // Player1 has an attacker
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // Creature can't block while pacified
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        // Remove Pacifism
        gd.playerBattlefields.get(player1.getId()).remove(pacifismPerm);

        // Now creature can block
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    // ===== Pacifism on own creature =====

    @Test
    @DisplayName("Pacifism can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, "W", 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pacifism")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Pacifism fizzles if target removed =====

    @Test
    @DisplayName("Pacifism fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, "W", 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove the target before Pacifism resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Pacifism should be in graveyard, not on battlefield
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pacifism"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pacifism"));
    }

    // ===== Declaring no attackers still works =====

    @Test
    @DisplayName("Player with only pacified creatures can declare no attackers")
    void canDeclareNoAttackersWithOnlyPacifiedCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent pacifismPerm = new Permanent(new Pacifism());
        pacifismPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifismPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        // Declaring no attackers should succeed
        gs.declareAttackers(gd, player1, List.of());

        assertThat(bearsPerm.isAttacking()).isFalse();
    }
}

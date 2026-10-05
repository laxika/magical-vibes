package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuminousBonds.class, WalkingCorpse.class, Manalith.class})
class LuminousBondsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Luminous Bonds attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new LuminousBonds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Luminous Bonds")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        bearsPerm.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LuminousBonds());
        aura.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as a blocker")
    void enchantedCreatureCannotBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blockerPerm.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LuminousBonds());
        aura.setAttachedTo(blockerPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Creature can attack again once Luminous Bonds leaves the battlefield")
    void creatureCanAttackAfterAuraRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        bearsPerm.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LuminousBonds());
        aura.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new LuminousBonds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Manalith");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Luminous Bonds fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new LuminousBonds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Luminous Bonds");
        harness.assertNotOnBattlefield(player1, "Luminous Bonds");
    }

    @Test
    @DisplayName("A resolved Luminous Bonds can enchant your creature and only restricts that creature")
    void ownCreatureIsRestrictedWhileOtherCreatureCanAttack() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        enchanted.setSummoningSick(false);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        other.setSummoningSick(false);
        harness.setHand(player1, List.of(new LuminousBonds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Luminous Bonds").getAttachedTo()).isEqualTo(enchanted.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(enchanted.isAttacking()).isFalse();
        assertThat(other.isAttacking()).isTrue();
    }
}

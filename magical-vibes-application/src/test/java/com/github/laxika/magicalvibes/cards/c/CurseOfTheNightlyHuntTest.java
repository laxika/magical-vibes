package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.m.ManorSkeleton;
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

@CardUsed({CurseOfTheNightlyHunt.class, WalkingCorpse.class, ManorSkeleton.class})
class CurseOfTheNightlyHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfTheNightlyHunt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving puts curse onto the battlefield attached to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfTheNightlyHunt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent curse = findPermanent(player1, "Curse of the Nightly Hunt");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Enchanted player's creatures must attack")
    void enchantedPlayerCreaturesMustAttack() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Declaring no attackers should fail because bears must attack
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Enchanted player's creatures can successfully be declared as attackers")
    void enchantedPlayerCreaturesCanAttack() {
        harness.setLife(player1, 20);

        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Curse controller's creatures are NOT affected")
    void doesNotAffectControllerCreatures() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Controller's bears should NOT be forced to attack
        gs.declareAttackers(gd, player1, List.of());
    }

    @Test
    @DisplayName("Tapped creatures are not forced to attack")
    void tappedCreaturesNotForced() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears.setSummoningSick(false);
        bears.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // A tapped creature cannot satisfy an attack requirement.
        gs.declareAttackers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Summoning sick creatures are not forced to attack")
    void summoningSickCreaturesNotForced() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        // Bears with summoning sickness
        harness.addToBattlefield(player2, new WalkingCorpse());

        // Another creature without summoning sickness that must attack
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears2.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only bears2 (index 1) must attack; bears (index 0) has summoning sickness
        gs.declareAttackers(gd, player2, List.of(1));
    }

    @Test
    @DisplayName("Must-attack effect is removed when curse leaves the battlefield")
    void effectRemovedWhenCurseLeaves() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears.setSummoningSick(false);

        // Remove curse
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == curse);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Now bears can choose not to attack
        gs.declareAttackers(gd, player2, List.of());
    }

    @Test
    @DisplayName("All of enchanted player's creatures must attack")
    void allCreaturesMustAttack() {
        CurseOfTheNightlyHunt curse = new CurseOfTheNightlyHunt();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears1.setSummoningSick(false);

        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        bears2.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Both creatures must attack.
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A player can enchant themselves and must attack with their creatures")
    void selfEnchantmentRequiresAttacking() {
        harness.setHand(player1, List.of(new CurseOfTheNightlyHunt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A newly controlled creature with haste must attack")
    void hasteCreatureMustAttackImmediately() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheNightlyHunt());
        curse.setAttachedTo(player2.getId());
        harness.addToBattlefield(player2, new ManorSkeleton());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        gs.declareAttackers(gd, player2, List.of(0));
        harness.assertLife(player1, 19);
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.a.AzoriusCluestone;
import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaaka;
import com.github.laxika.magicalvibes.cards.p.PunishTheEnemy;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaviniaOfTheTenth.class, GrizzlyBears.class, SerraAngel.class, LlanowarElves.class,
        AzoriusCluestone.class, AzoriusGuildgate.class, RubblebeltMaaka.class, PunishTheEnemy.class})
class LaviniaOfTheTenthTest extends BaseCardTest {

    @Test
    @DisplayName("Detains an opponent's cheap creature so it can't attack")
    void detainsCheapOpponentCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLavinia();

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained permanent's activated abilities can't be activated")
    void detainedPermanentCantActivateAbilities() {
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        castLavinia();

        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, elves)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Permanents with mana value 5 or more are unaffected")
    void expensivePermanentsAreUnaffected() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castLavinia();

        assertThatCode(() -> declareAttack(angel)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Your own permanents are not detained")
    void ownPermanentsAreNotDetained() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);

        castLavinia();

        assertThatCode(() -> harness.tapPermanent(player1, indexOf(player1, elves)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain wears off at Lavinia's controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLavinia();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    void detainsMultiplePermanentsIncludingManaValueFour() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent maaka = harness.addToBattlefieldAndReturn(player2, new RubblebeltMaaka());

        castLavinia();

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThatThrownBy(() -> declareAttack(maaka))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void detainedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLavinia();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    void detainedArtifactCannotActivateManaOrNonmanaAbilities() {
        Permanent cluestone = harness.addToBattlefieldAndReturn(player2, new AzoriusCluestone());
        castLavinia();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, cluestone), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, cluestone), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void opponentLandIsNotDetained() {
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new AzoriusGuildgate());
        gate.untap();
        castLavinia();

        assertThatCode(() -> harness.activateAbility(player2, indexOf(player2, gate), 0, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void laterPermanentsAreNotDetained() {
        castLavinia();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    void detainDoesNotExpireAtOpponentsTurnStart() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLavinia();
        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void detainPersistsAfterLaviniaLeavesBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLavinia();
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void protectionFromRedPreventsBlockingAfterDetainExpires() {
        Permanent maaka = harness.addToBattlefieldAndReturn(player2, new RubblebeltMaaka());
        castLavinia();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        Permanent lavinia = gd.playerBattlefields.get(player1.getId()).getFirst();
        lavinia.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, maaka), indexOf(player1, lavinia)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionFromRedRejectsRedSpellTarget() {
        castLavinia();
        Permanent lavinia = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new PunishTheEnemy()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(player1.getId(), lavinia.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionFromRedPreventsCombatDamage() {
        castLavinia();
        Permanent lavinia = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent maaka = harness.addToBattlefieldAndReturn(player2, new RubblebeltMaaka());
        maaka.setSummoningSick(false);
        maaka.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, lavinia), indexOf(player2, maaka))));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(lavinia.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Lavinia of the Tenth");
        harness.assertInGraveyard(player2, "Rubblebelt Maaka");
    }

    @Test
    void nonredCreatureCanBlockAfterDetainExpires() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLavinia();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        Permanent lavinia = gd.playerBattlefields.get(player1.getId()).getFirst();
        lavinia.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, bear), indexOf(player1, lavinia)))))
                .doesNotThrowAnyException();
    }
    private void castLavinia() {
        harness.castFromHand(player1, new LaviniaOfTheTenth(), "{3}{W}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(indexOf(player2, creature)));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

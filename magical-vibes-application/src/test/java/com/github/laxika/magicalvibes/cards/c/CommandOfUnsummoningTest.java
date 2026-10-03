package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandOfUnsummoning.class, GrizzlyBears.class, JaceBeleren.class})
class CommandOfUnsummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target attacking creatures to their owner's hand")
    void returnsTwoAttackers() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttackerTargeting(player1, player2);
        Permanent a2 = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, List.of(a1.getId(), a2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(a1.getCard(), a2.getCard());
    }

    @Test
    @DisplayName("Returns a single target attacking creature to its owner's hand")
    void returnsOneAttacker() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttackerTargeting(player1, player2);
        Permanent a2 = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, List.of(a1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(a2);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(a1.getCard())
                .doesNotContain(a2.getCard());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent idle = idleCreature(player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(idle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same attacking creature twice")
    void cannotTargetSameAttackerTwice() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                List.of(attacker.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(a1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast when you have not been attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        harness.setHand(player1, List.of(new CommandOfUnsummoning()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not return a creature that stops attacking before resolution")
    void doesNotReturnCreatureThatStopsAttackingBeforeResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, List.of(attacker.getId()));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(attacker.getCard());
    }

    @Test
    @DisplayName("Returns the remaining legal target when the other creature stops attacking")
    void returnsRemainingLegalTarget() {
        harness.forceActivePlayer(player1);
        Permanent first = addAttackerTargeting(player1, player2);
        Permanent second = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, List.of(first.getId(), second.getId()));
        first.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(second.getCard()).doesNotContain(first.getCard());
    }

    @Test
    @CardUsed({CommandOfUnsummoning.class, GrizzlyBears.class, JaceBeleren.class})
    @DisplayName("Having been attacked still permits casting after that attacker leaves combat")
    void canCastAfterLastCreatureAttackingPlayerLeavesCombat() {
        Permanent atPlayer = addCreatureReady(player1, new GrizzlyBears());
        Permanent atPlaneswalker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1),
                Map.of(0, player2.getId(), 1, planeswalker.getId()));

        atPlayer.setAttacking(false);
        harness.castAndResolveInstant(player2, 0, List.of(atPlaneswalker.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(atPlayer);
        assertThat(gd.playerHands.get(player1.getId())).contains(atPlaneswalker.getCard());
    }

    @Test
    @DisplayName("Cannot cast with no targets")
    void cannotCastWithNoTargets() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast with three targets")
    void cannotCastWithThreeTargets() {
        harness.forceActivePlayer(player1);
        Permanent first = addAttackerTargeting(player1, player2);
        Permanent second = addAttackerTargeting(player1, player2);
        Permanent third = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new CommandOfUnsummoning()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new GrizzlyBears());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent idleCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}

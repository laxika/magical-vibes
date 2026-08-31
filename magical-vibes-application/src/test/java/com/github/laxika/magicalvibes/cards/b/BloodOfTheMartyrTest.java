package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodOfTheMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage to any creature to the spell's controller")
    void redirectsDamageToAnyCreature() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        castBloodOfTheMartyr();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The controller can decline redirecting a creature's damage")
    void canDeclineRedirectingDamage() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        castBloodOfTheMartyr();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());

        castBloodOfTheMartyr();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Redirects combat damage that would be dealt to a creature")
    void redirectsCombatDamageToController() {
        harness.setLife(player1, 20);
        Permanent blocker = addReadyPermanent(player1, new GrizzlyBears());
        Permanent attacker = addReadyPermanent(player2, new HillGiant());

        castBloodOfTheMartyr();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(indexOf(player2, attacker)));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, blocker), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    private void castBloodOfTheMartyr() {
        harness.setHand(player1, List.of(new BloodOfTheMartyr()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                        com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarshJustice.class, GrizzlyBears.class, JaceBeleren.class})
class HarshJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creature that deals combat damage to you reflects it to its controller")
    void reflectsCombatDamageToAttackerController() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int attackerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.stack).isEmpty();

        resolveCombat(player1);
        resolveAllTriggers();

        // player2 took 2 combat damage; the Grizzly Bears reflected 2 back to player1.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore - 2);
    }

    @Test
    @DisplayName("Blocked attacker deals no combat damage to you, so nothing is reflected")
    void blockedAttackerDoesNotReflect() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int attackerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        // No combat damage to a player — no reflection, no life loss for either player.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore);
    }

    @Test
    @DisplayName("Each attacking creature reflects its own combat damage")
    void reflectsCombatDamageFromEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int attackerLifeBefore = gd.getLife(player1.getId());
        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0);
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore - 4);
    }

    @Test
    @DisplayName("Reflection expires at the end of the turn")
    void reflectionExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0);
        resolveCombat(player1);
        resolveAllTriggers();

        int player1LifeAfterFirstCombat = gd.getLife(player1.getId());
        int player2LifeAfterFirstCombat = gd.getLife(player2.getId());

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeAfterFirstCombat - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeAfterFirstCombat);
    }

    @Test
    @DisplayName("Cannot cast if not attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast if only a planeswalker was attacked")
    void cannotCastWhenOnlyPlaneswalkerWasAttacked() {
        harness.forceActivePlayer(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent attacker = addAttacker(player1, player2);
        attacker.setAttackTarget(jace.getId());
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new HarshJustice()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new GrizzlyBears());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}

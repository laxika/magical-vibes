package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RagingRegisaur;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DeepWood.class, GrizzlyBears.class, WildGriffin.class})
class DeepWoodTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from an attacking creature to you is prevented")
    void preventsCombatDamageFromAttacker() {
        harness.forceActivePlayer(player1);
        addAttacker();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player2, new DeepWood(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        // Advance to combat damage: the unblocked Wild Griffin would deal 2 to player2.
        resolveCombat();

        // All damage from the attacking creature is prevented.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Damage from an attacking creature is prevented even when it is noncombat damage")
    @CardUsed(RagingRegisaur.class)
    void preventsNoncombatDamageFromAttackingCreature() {
        addCreatureReady(player1, new RagingRegisaur());
        declareAttackers(List.of(0));

        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.castFromHand(player2, new DeepWood(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Prevents damage to you but not damage to your creatures")
    void preventsOnlyDamageToController() {
        harness.forceActivePlayer(player1);
        addAttacker();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        addCreatureReady(player2, new WildGriffin());

        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player2, new DeepWood(), "{1}{G}");
        harness.passBothPriorities();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
        harness.assertNotOnBattlefield(player2, "Wild Griffin");
    }

    @Test
    @DisplayName("Cannot cast if not attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new DeepWood(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new DeepWood(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttacker() {
        Permanent perm = addCreatureReady(player1, new WildGriffin());
        perm.tap();
        perm.setAttacking(true);
        perm.setAttackTarget(player2.getId());
        return perm;
    }

    @Test
    @CardUsed({ProdigalSorcerer.class})
    @DisplayName("Noncombat damage from a nonattacking creature is not prevented")
    void doesNotPreventNoncombatDamageFromNonAttacker() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int defenderLifeBefore = gd.getLife(player2.getId());

        // Put the nonattacking creature's ability below Deep Wood so it resolves while the
        // declare attackers step is still active.
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.castFromHand(player2, new DeepWood(), "{1}{G}");
        harness.passBothPriorities();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 1);
    }

    private Permanent addAttacker(Player attackerController, Player defender, Card card) {
        Permanent perm = addCreatureReady(attackerController, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}

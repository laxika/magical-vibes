package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScorchingWinds.class, GrizzlyBears.class, RagingGoblin.class, WallOfGranite.class, JaceBeleren.class})
class ScorchingWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttacker(player1, player2, new GrizzlyBears());
        Permanent a2 = addAttacker(player1, player2, new GrizzlyBears());
        castScorchingWinds();

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new RagingGoblin());
        castScorchingWinds();

        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        Permanent idle = addCreatureReady(player1, new WallOfGranite());
        castScorchingWinds();

        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot cast when not attacked this step")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new ScorchingWinds(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("Cannot cast when only a planeswalker you control was attacked")
    void cannotCastWhenOnlyPlaneswalkerWasAttacked() {
        harness.forceActivePlayer(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        attacker.setAttackTarget(jace.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new ScorchingWinds(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new ScorchingWinds(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Entering attacking does not count as having attacked the player")
    void cannotCastWhenCreatureOnlyEnteredAttacking() {
        harness.forceActivePlayer(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.enterAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new ScorchingWinds(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast after all declared attackers have left combat")
    void canCastAfterDeclaredAttackerLeavesCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        castScorchingWinds();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Scorching Winds");
    }

    @Test
    @DisplayName("Only creatures still attacking at resolution take damage")
    void checksAttackingStateAtResolution() {
        harness.forceActivePlayer(player1);
        Permanent remaining = addAttacker(player1, player2, new GrizzlyBears());
        Permanent removed = addAttacker(player1, player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player2, new ScorchingWinds(), "{R}");
        removed.setAttacking(false);
        removed.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
        assertThat(removed.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("Also damages creatures attacking a planeswalker when the player was attacked")
    void damagesPlaneswalkerAttackersToo() {
        harness.forceActivePlayer(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent direct = addAttacker(player1, player2, new GrizzlyBears());
        Permanent planeswalkerAttacker = addAttacker(player1, player2, new GrizzlyBears());
        planeswalkerAttacker.setAttackTarget(jace.getId());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        castScorchingWinds();

        assertThat(direct.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalkerAttacker.getMarkedDamage()).isEqualTo(1);
        assertThat(defender.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castScorchingWinds() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player2, new ScorchingWinds(), "{R}");
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}

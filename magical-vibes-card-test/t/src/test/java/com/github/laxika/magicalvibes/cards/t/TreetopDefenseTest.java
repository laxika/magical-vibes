package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TreetopDefense.class, GrizzlyBears.class})
class TreetopDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: your creatures gain reach")
    void grantsReachWhenAttacked() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        Permanent myCreature = creature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new TreetopDefense(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, myCreature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Reach affects current creatures only and wears off at end of turn")
    void reachGrantExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent currentCreature = creature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new TreetopDefense(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, currentCreature, Keyword.REACH)).isTrue();

        Permanent laterCreature = creature(player2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.REACH)).isFalse();

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, currentCreature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new TreetopDefense(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castFromHand(player2, new TreetopDefense(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast after the last declared attacker leaves during the same step")
    void canCastAfterLastAttackerLeaves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = creature(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(attacker.getAttackTarget()).isEqualTo(player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.castFromHand(player2, new TreetopDefense(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, defender, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before resolution also gain reach")
    void includesCreaturesEnteringBeforeResolution() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent firstCreature = creature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player2, new TreetopDefense(), "{1}{G}");

        Permanent secondCreature = creature(player2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.REACH)).isTrue();
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new GrizzlyBears());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent creature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}

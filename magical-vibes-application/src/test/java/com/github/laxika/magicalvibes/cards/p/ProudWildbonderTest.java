package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HootingMandrills;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProudWildbonder.class, GrizzlyBears.class, HootingMandrills.class})
class ProudWildbonderTest extends BaseCardTest {

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToBlockers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    @DisplayName("Proud Wildbonder lets your trample creature assign unblocked damage to a defending creature")
    void trampleCreatureMayAssignDamageToDefendingCreature() {
        harness.setLife(player2, 20);
        Permanent bonder = addReadyAttacker(player1, new ProudWildbonder());
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        advanceToBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(bonder.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Proud Wildbonder does not grant the ability to a creature without trample")
    void nonTrampleCreatureDoesNotGetAbility() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ProudWildbonder());
        addReadyAttacker(player1, new GrizzlyBears());
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        advanceToBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Proud Wildbonder does not grant the ability to an opponent's trample creature")
    void opponentTrampleCreatureDoesNotGetAbility() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProudWildbonder());
        Permanent defendingCreature = new Permanent(new GrizzlyBears());
        defendingCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(defendingCreature);
        addReadyAttacker(player2, new HootingMandrills());

        advanceToBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }
}

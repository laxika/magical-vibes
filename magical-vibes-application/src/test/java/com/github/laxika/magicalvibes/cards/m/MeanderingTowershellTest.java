package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeanderingTowershell.class, AlpineGrizzly.class, Island.class})
class MeanderingTowershellTest extends BaseCardTest {

    private Permanent addReadyTowershell() {
        return addCreatureReady(player1, new MeanderingTowershell());
    }

    @Test
    @DisplayName("Attacking exiles Meandering Towershell")
    void attackingExilesIt() {
        Permanent towershell = addReadyTowershell();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(towershell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(towershell.getCard());
    }

    @Test
    @DisplayName("Meandering Towershell returns tapped and attacking on its controller's next turn")
    void returnsTappedAndAttackingOnNextTurn() {
        Permanent towershell = addReadyTowershell();
        addCreatureReady(player1, new AlpineGrizzly());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        gd.turnNumber++;
        beginAnotherCombat();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(towershell.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of()));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Meandering Towershell");
        assertThat(returned.getCard().getId()).isEqualTo(towershell.getCard().getId());
        assertThat(returned).isNotSameAs(towershell);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(towershell.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void extraCombatOnSameTurnDoesNotReturnIt() {
        Permanent towershell = addReadyTowershell();
        addCreatureReady(player1, new AlpineGrizzly());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        beginAnotherCombat();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(towershell.getCard());
        assertThat(findPermanents(player1, "Meandering Towershell")).isEmpty();
    }

    @Test
    void leavingExileAndBeingExiledAgainDoesNotReturnNewObject() {
        Permanent towershell = addReadyTowershell();
        addCreatureReady(player1, new AlpineGrizzly());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.removeFromExile(towershell.getCard().getId())).isTrue();
        harness.addToBattlefield(player1, towershell.getCard());
        harness.getPermanentRemovalService().removePermanentToExile(gd,
                findPermanent(player1, "Meandering Towershell"));

        gd.turnNumber++;
        beginAnotherCombat();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(towershell.getCard());
        assertThat(findPermanents(player1, "Meandering Towershell")).isEmpty();
    }

    @Test
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        Permanent towershell = addReadyTowershell();
        towershell.setAttacking(true);
        towershell.setAttackTarget(player2.getId());
        addCreatureReady(player2, new AlpineGrizzly());
        harness.addToBattlefield(player2, new Island());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void islandControlledByAttackerDoesNotPreventBlocking() {
        Permanent towershell = addReadyTowershell();
        towershell.setAttacking(true);
        towershell.setAttackTarget(player2.getId());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void beginAnotherCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
    }
}

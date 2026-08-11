package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeanderingTowershellTest extends BaseCardTest {

    private Permanent addReadyTowershell() {
        Permanent towershell = new Permanent(new MeanderingTowershell());
        towershell.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(towershell);
        return towershell;
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
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(towershell.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned).isNotSameAs(towershell);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
    }
}

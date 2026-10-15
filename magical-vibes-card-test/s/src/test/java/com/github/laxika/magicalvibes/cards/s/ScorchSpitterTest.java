package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScorchSpitter.class, ChandraNovicePyromancer.class, GreenwoodSentinel.class})
class ScorchSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking triggers 1 damage to the player being attacked")
    void attackingDamagesPlayerBeingAttacked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScorchSpitter());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getAttackedTargetId()).isEqualTo(player2.getId());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking a planeswalker triggers 1 damage to that planeswalker")
    void attackingDamagesPlaneswalkerBeingAttacked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScorchSpitter());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ChandraNovicePyromancer());

        declareAttackAgainst(planeswalker);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when another creature attacks")
    void doesNotTriggerForAnotherCreature() {
        addCreatureReady(player1, new ScorchSpitter());
        addCreatureReady(player1, new GreenwoodSentinel());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack damage still resolves after Scorch Spitter leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent spitter = addCreatureReady(player1, new ScorchSpitter());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(spitter);
        gd.playerGraveyards.get(player1.getId()).add(spitter.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A departed attacked planeswalker does not redirect the damage to its controller")
    void departedPlaneswalkerDoesNotDamageController() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScorchSpitter());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ChandraNovicePyromancer());

        declareAttackAgainst(planeswalker);
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void declareAttackAgainst(Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScorchSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking triggers 1 damage to the player being attacked")
    void attackingDamagesPlayerBeingAttacked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScorchSpitter());

        declareAttackers(player1, List.of(0), null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getAttackedTargetId()).isEqualTo(player2.getId());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking a planeswalker triggers 1 damage to that planeswalker")
    void attackingDamagesPlaneswalkerBeingAttacked() {
        addCreatureReady(player1, new ScorchSpitter());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when another creature attacks")
    void doesNotTriggerForAnotherCreature() {
        addCreatureReady(player1, new ScorchSpitter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1), null);

        assertThat(gd.stack).isEmpty();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

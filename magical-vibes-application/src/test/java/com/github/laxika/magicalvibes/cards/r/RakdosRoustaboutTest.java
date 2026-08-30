package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RakdosRoustaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the player it is attacking when it becomes blocked")
    void dealsDamageToAttackedPlayerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent roustabout = addRoustabout(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0), null);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(roustabout.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to the planeswalker it is attacking when it becomes blocked")
    void dealsDamageToAttackedPlaneswalkerWhenBlocked() {
        Permanent roustabout = addRoustabout(player1);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(roustabout.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when it is not blocked")
    void doesNotTriggerWhenUnblocked() {
        harness.setLife(player2, 20);
        addRoustabout(player1);

        declareAttackers(player1, List.of(0), null);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addRoustabout(Player player) {
        Card card = new RakdosRoustabout();
        card.setPower(3);
        card.setToughness(2);
        return addCreatureReady(player, card);
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

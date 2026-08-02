package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeechingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player loses 1 life when a Sliver attacks")
    void triggersWhenSliverAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addLeechingSliverReady(player1);
        addSliverCreatureReady(player1);

        declareAttackers(List.of(1)); // 2/2 Sliver attacks

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leeching Sliver");

        harness.passBothPriorities();

        // 20 - 1 (trigger) - 2 (combat damage) = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leeching Sliver is itself a Sliver — triggers when it attacks")
    void triggersWhenItselfAttacks() {
        harness.setLife(player2, 20);

        addLeechingSliverReady(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // 20 - 1 (trigger) - 1 (1/1 combat damage) = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triggers once per attacking Sliver")
    void triggersPerSliver() {
        harness.setLife(player2, 20);

        addLeechingSliverReady(player1);
        addSliverCreatureReady(player1);
        addSliverCreatureReady(player1);

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // 20 - 2 (triggers) - 4 (two 2/2s) = 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not trigger when a non-Sliver attacks")
    void doesNotTriggerForNonSliver() {
        addLeechingSliverReady(player1);
        addNonSliverCreatureReady(player1);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's attacking Sliver")
    void doesNotTriggerForOpponentSliver() {
        addLeechingSliverReady(player1);
        addSliverCreatureReady(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Leeching Sliver"))
                .count()).isZero();
    }

    private Permanent addLeechingSliverReady(Player player) {
        Permanent perm = new Permanent(new LeechingSliver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSliverCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Sliver");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}");
        creature.setSubtypes(List.of(CardSubtype.SLIVER));
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addNonSliverCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

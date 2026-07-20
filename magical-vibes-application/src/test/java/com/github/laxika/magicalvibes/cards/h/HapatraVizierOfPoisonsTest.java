package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HapatraVizierOfPoisonsTest extends BaseCardTest {

    private long snakeCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snake"))
                .count();
    }

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }

    private Permanent attackWithHapatra(Player player) {
        Permanent hapatra = addCreatureReady(player, new HapatraVizierOfPoisons());
        hapatra.setAttacking(true);
        return hapatra;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage: may put a -1/-1 counter on target creature, which makes a Snake")
    void combatDamagePutsCounterAndCreatesSnake() {
        attackWithHapatra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombat();

        // Resolution-time "you may" for the combat trigger.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        // Then choose any creature to receive the -1/-1 counter.
        harness.handlePermanentChosen(player1, bearsId);
        resolveStack();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        // The -1/-1 counter placement triggers the second ability once: one Snake for Hapatra's controller.
        assertThat(snakeCount(player1)).isEqualTo(1);
        assertThat(snakeCount(player2)).isZero();
    }

    @Test
    @DisplayName("Declining the combat trigger places no counter and makes no Snake")
    void decliningCombatTriggerDoesNothing() {
        attackWithHapatra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveStack();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(snakeCount(player1)).isZero();
    }

    @Test
    @DisplayName("Multiple -1/-1 counters on one creature at once make only a single Snake")
    void multipleCountersAtOnceMakeOneSnake() {
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());
        // 4/4 survives three -1/-1 counters (becomes 1/1), so no death interferes.
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // player1 casts Skinrender → player1 puts three -1/-1 counters in one instance.
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveStack();

        Permanent airElemental = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(airElemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        // Once per creature per instance — not once per counter.
        assertThat(snakeCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Hapatra")
    void opponentPlacingCountersDoesNotTrigger() {
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());
        // The creature receiving the counters belongs to player1 so player2's Skinrender has a target.
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, targetId, null);
        resolveStack();

        assertThat(snakeCount(player1)).isZero();
    }
}

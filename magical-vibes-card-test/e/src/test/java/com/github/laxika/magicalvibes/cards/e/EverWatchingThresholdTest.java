package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverWatchingThreshold.class, GideonBlackblade.class, GrizzlyBears.class})
class EverWatchingThresholdTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card when an opponent attacks with multiple creatures")
    void drawsOnceForMultipleAttackers() {
        harness.addToBattlefield(player1, new EverWatchingThreshold());
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player2, List.of(0, 1), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an opponent attacks a planeswalker you control")
    void drawsForPlaneswalkerAttack() {
        harness.addToBattlefield(player1, new EverWatchingThreshold());
        Permanent planeswalker = addPlaneswalker(player1);
        addReadyCreature(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the controller attacks")
    void doesNotTriggerForControllerAttack() {
        harness.addToBattlefield(player1, new EverWatchingThreshold());
        addReadyCreature(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1), null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = new Permanent(new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}

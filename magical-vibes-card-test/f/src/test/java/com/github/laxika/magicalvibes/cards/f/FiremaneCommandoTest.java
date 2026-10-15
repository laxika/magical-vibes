package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiremaneCommando.class, GrizzlyBears.class, LightningBolt.class, ElspethSunsChampion.class})
class FiremaneCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when you attack with two creatures")
    void drawsWhenYouAttackWithTwoCreatures() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LightningBolt()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent draws when attacking a planeswalker you control")
    void opponentDrawsWhenAttackingYourPlaneswalker() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LightningBolt()));

        declareAttackers(player2, List.of(0, 1), Map.of(
                0, planeswalker.getId(),
                1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not let an opponent draw when any attacker attacks you directly")
    void opponentDoesNotDrawWhenAnAttackerAttacksYouDirectly() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LightningBolt()));

        declareAttackers(player2, List.of(0, 1), Map.of(
                0, player1.getId(),
                1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("One opposing attacker does not trigger a draw ability")
    void singleAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LightningBolt()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Three attackers draw exactly one card, including when Commando attacks")
    void threeAttackersDrawOnce() {
        addCreatureReady(player1, new FiremaneCommando());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));

        declareAttackers(player1, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent attacking you still triggers, but does not draw")
    void attackingYouStillTriggersWithoutDrawing() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LightningBolt()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(player2, List.of(0, 1)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent draw survives the attacking creatures leaving combat")
    void opponentDrawSurvivesAttackersLeavingCombat() {
        harness.addToBattlefield(player1, new FiremaneCommando());
        Permanent planeswalker = addPlaneswalker(player1);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LightningBolt()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(player2, List.of(0, 1), Map.of(
                        0, planeswalker.getId(), 1, planeswalker.getId())));
        first.setAttacking(false);
        second.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Commando attacking alone does not draw")
    void commandoAttackingAloneDoesNotDraw() {
        addCreatureReady(player1, new FiremaneCommando());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LightningBolt()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ElspethSunsChampion());
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        return permanent;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}

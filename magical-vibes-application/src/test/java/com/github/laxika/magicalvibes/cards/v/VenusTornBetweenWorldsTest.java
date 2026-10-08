package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JudgmentBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenusTornBetweenWorlds.class, Forest.class, GrizzlyBears.class, JudgmentBolt.class, Shock.class})
class VenusTornBetweenWorldsTest extends BaseCardTest {

    @Test
    void putsCountersEqualToDamageIfItSurvives() {
        Permanent venus = harness.addToBattlefieldAndReturn(player2, new VenusTornBetweenWorlds());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, venus.getId());
        harness.passBothPriorities();

        assertThat(venus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(venus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPutCountersAfterLethalDamage() {
        Permanent venus = harness.addToBattlefieldAndReturn(player2, new VenusTornBetweenWorlds());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, venus.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Venus, Torn Between Worlds");
        harness.passBothPriorities();
        assertThat(venus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayPayBlueToDrawWhenCounteredCreatureDealsCombatDamage() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerForCreatureWithoutCounters() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void putsCountersOnVenusAfterSurvivingCombatDamage() {
        Permanent venus = addCreatureReady(player1, new VenusTornBetweenWorlds());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        venus.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Venus, Torn Between Worlds");
        assertThat(venus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void venusCanTriggerItsOwnDrawAbilityWithANonStatCounter() {
        Permanent venus = addCreatureReady(player1, new VenusTornBetweenWorlds());
        venus.setCounterCount(CounterType.CHARGE, 1);
        venus.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void mayDeclineToPayWithoutDrawing() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerForOpponentsCreatureWithCounters() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void removingCountersAfterCombatDamageDoesNotUndoDrawTrigger() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
    @Test
    void eachCounteredAttackerTriggersASeparateOptionalDraw() {
        harness.addToBattlefield(player1, new VenusTornBetweenWorlds());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.CHARGE, 1);
        first.setAttacking(true);
        second.setAttacking(true);
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void drawsNothingForNoncombatDamageToAPlayer() {
        Permanent venus = harness.addToBattlefieldAndReturn(player1, new VenusTornBetweenWorlds());
        venus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
    private void resolveCombatToMayPrompt() {
        resolveCombat();
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InexorableTide.class, GrizzlyBears.class, Spellbook.class})
class InexorableTideTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell triggers proliferate")
    void castingSpellTriggersProliferate() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Triggered ability should be on the stack
        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Inexorable Tide"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve triggered ability (proliferate)

        // Choose the bears for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger proliferate")
    void opponentCastingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        // Only the Spellbook spell should be on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isNotEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds -1/-1 counters to chosen permanents")
    void proliferateAddsMinusCounters() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        harness.addToBattlefield(player1, new InexorableTide());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // No MULTI_PERMANENT_CHOICE — no eligible permanents
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Proliferate adds every existing counter kind to selected permanents and players")
    void proliferatesPermanentsAndPlayersTogether() {
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new InexorableTide());
        tide.setCounterCount(CounterType.CHARGE, 2);
        tide.setCounterCount(CounterType.TIME, 1);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(tide.getId(), player2.getId()));

        assertThat(tide.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(tide.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(tide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Players can be proliferated when no permanent has counters")
    void proliferatesPlayerWithoutEligiblePermanents() {
        harness.addToBattlefield(player1, new InexorableTide());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Casting Inexorable Tide does not trigger its own ability")
    void doesNotTriggerOnItsOwnCast() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InexorableTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inexorable Tide");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A proliferate trigger resolves after Inexorable Tide leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new InexorableTide());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(tide);
        gd.playerGraveyards.get(player1.getId()).add(tide.getCard());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Inexorable Tide");
    }

    @Test
    @DisplayName("Triggers proliferate for each spell cast")
    void triggersForEachSpellCast() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first spell
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve proliferate trigger
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve Spellbook spell

        // Cast second spell
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve proliferate trigger
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GrimAffliction.class, GrizzlyBears.class, Spellbook.class})
class GrimAfflictionTest extends BaseCardTest {


    @Test
    @DisplayName("Puts -1/-1 counter on target creature and then proliferates")
    void putsCounterAndProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature got a -1/-1 counter (now 1/1)
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // Proliferate choice is now awaited — choose both permanents with counters
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), otherBears.getId()));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(otherBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts counter and proliferates choosing none")
    void putsCounterAndProliferatesNone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature got a -1/-1 counter
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // Proliferate — choose none
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no proliferate either
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(otherBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }


    @Test
    @DisplayName("Cannot target non-creature permanents")
    void cannotTargetNonCreature() {
        UUID spellbookId = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Proliferate — choose the target (it has a counter now)
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grim Affliction");
    }

    @Test
    @DisplayName("Proliferates each counter kind on selected permanents and players")
    void proliferatesPermanentAndPlayerCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        book.setCounterCount(CounterType.CHARGE, 2);
        book.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(book.getId(), player2.getId()));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(book.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(book.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature reduced to zero toughness remains available for proliferate")
    void zeroToughnessCreatureRemainsUntilResolutionFinishes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opposing power and toughness counters are not canceled before proliferating")
    void opposingCountersRemainUntilProliferationCompletes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}

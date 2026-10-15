package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContagionClasp.class, CarapaceForger.class, AccordersShield.class})
class ContagionClaspTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player2, new CarapaceForger());
        UUID forgerId = harness.getPermanentId(player2, "Carapace Forger");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, forgerId);
        resolveAllTriggers();

        Permanent forger = findPermanent(player2, "Carapace Forger");
        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(forger.getEffectivePower()).isEqualTo(1);
        assertThat(forger.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can kill a 1/1 creature")
    void etbKillsOneOneCreature() {
        // Create a 1/1 by giving a 2/2 Carapace Forger a -1/-1 counter
        Permanent forger = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        forger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        UUID forgerId = forger.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, forgerId);
        resolveAllTriggers();

        // Forger (1/1) got another -1/-1 counter making it 0/0, dies to SBA
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Proliferate adds another -1/-1 counter to chosen creature")
    void proliferateAddsMinusCounters() {
        addReadyClasp(player1);
        Permanent forger = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        forger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // Now awaiting proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(forger.getId()));

        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds another +1/+1 counter to chosen creature")
    void proliferateAddsPlusCounters() {
        addReadyClasp(player1);
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        forger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forger.getId()));

        assertThat(forger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose none (empty selection)")
    void proliferateCanChooseNone() {
        addReadyClasp(player1);
        Permanent forger = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        forger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds counters to multiple permanents")
    void proliferateMultiplePermanents() {
        addReadyClasp(player1);

        Permanent forger1 = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        forger1.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        Permanent forger2 = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        forger2.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forger1.getId(), forger2.getId()));

        assertThat(forger1.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(forger2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate kills creature with 0 toughness from additional -1/-1 counter")
    void proliferateKillsCreature() {
        addReadyClasp(player1);

        // Carapace Forger (2/2) with 1 -1/-1 counter = 1/1, another makes it 0/0 → dies
        Permanent forger = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        forger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forger.getId()));

        // Forger should be dead (0/0 from SBA)
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        addReadyClasp(player1);
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No MULTI_PERMANENT_CHOICE should be awaited — no eligible permanents
        Permanent forger = findPermanent(player2, "Carapace Forger");
        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-creature permanents without counters are not eligible for proliferate")
    void nonCreaturePermanentsWithoutCountersNotEligible() {
        addReadyClasp(player1);
        harness.addToBattlefield(player1, new AccordersShield());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No eligible permanents, no choice needed
        Permanent shield = findPermanent(player1, "Accorder's Shield");
        assertThat(shield.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Proliferate requires tap (cannot activate when tapped)")
    void proliferateRequiresTap() {
        Permanent clasp = addReadyClasp(player1);
        clasp.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can put its counter on a creature you control")
    void etbCanAffectOwnCreature() {
        Permanent forger = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, forger.getId());
        resolveAllTriggers();

        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Contagion Clasp");
    }

    @Test
    @DisplayName("Proliferate adds each existing counter kind to a noncreature permanent")
    void proliferateAddsEveryCounterKind() {
        Permanent clasp = addReadyClasp(player1);
        clasp.setCounterCount(CounterType.CHARGE, 2);
        clasp.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        unchosen.setCounterCount(CounterType.CHARGE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(clasp.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(clasp.getId()));

        assertThat(clasp.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(clasp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(clasp.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate can choose players and a permanent together")
    void proliferatePlayersAndPermanentTogether() {
        Permanent clasp = addReadyClasp(player1);
        clasp.setCounterCount(CounterType.CHARGE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(clasp.getId(), player2.getId()));

        assertThat(clasp.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate offers a choice when only players have counters")
    void proliferatePlayerWithoutPermanentCounters() {
        addReadyClasp(player1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Clasp enters even when there are no creatures to target")
    void entersWithoutCreatureTargets() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Contagion Clasp");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Proliferate can be activated during an opponent's turn")
    void proliferateDuringOpponentsTurn() {
        Permanent clasp = addReadyClasp(player1);
        clasp.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(clasp.getId()));

        assertThat(clasp.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate requires four mana")
    void proliferateRequiresFourMana() {
        Permanent clasp = addReadyClasp(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
        assertThat(clasp.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyClasp(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ContagionClasp());
    }
}

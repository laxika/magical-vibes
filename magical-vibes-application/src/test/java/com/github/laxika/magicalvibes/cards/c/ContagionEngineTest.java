package com.github.laxika.magicalvibes.cards.c;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContagionEngine.class, CarapaceForger.class})
class ContagionEngineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts -1/-1 counter on each creature target opponent controls")
    void etbPutsCountersOnAllOpponentCreatures() {
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> opponentBattlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : opponentBattlefield) {
            if (p.getCard().getName().equals("Carapace Forger")) {
                assertThat(p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
                assertThat(p.getEffectivePower()).isEqualTo(1);
                assertThat(p.getEffectiveToughness()).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("ETB does not affect controller's creatures")
    void etbDoesNotAffectControllerCreatures() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Controller's creature should be unaffected
        Permanent ownBears = findPermanent(player1, "Carapace Forger");
        assertThat(ownBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);

        // Opponent's creature should have a counter
        Permanent oppBears = findPermanent(player2, "Carapace Forger");
        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not affect non-creature permanents")
    void etbDoesNotAffectNonCreatures() {
        harness.addToBattlefield(player2, new ContagionEngine());
        harness.addToBattlefield(player2, new CarapaceForger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent nonCreature = findPermanent(player2, "Contagion Engine");
        assertThat(nonCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB kills 1/1 creatures with -1/-1 counter")
    void etbKillsOneOneCreatures() {
        Permanent weakBears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        weakBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1); // 2/2 with one -1/-1 = 1/1

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Forger (1/1) got another -1/-1 counter making it 0/0, dies to SBA
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Proliferate twice adds two -1/-1 counters to chosen creature")
    void proliferateTwiceAddsDoubleCounters() {
        addReadyEngine(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // First proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Second proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate twice can choose different permanents each time")
    void proliferateTwiceCanChooseDifferentTargets() {
        addReadyEngine(player1);

        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        bears1.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears2.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // First proliferate: choose bears1 only
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        // Second proliferate: choose bears2 only
        harness.handleMultiplePermanentsChosen(player1, List.of(bears2.getId()));

        assertThat(bears1.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate twice can choose none for both")
    void proliferateTwiceCanChooseNone() {
        addReadyEngine(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing for both proliferates
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate twice requires tap (cannot activate when tapped)")
    void proliferateTwiceRequiresTap() {
        Permanent engine = addReadyEngine(player1);
        engine.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Proliferate twice kills creature when counters bring toughness to zero")
    void proliferateTwiceKillsCreature() {
        addReadyEngine(player1);

        // Carapace Forger (2/2) with 1 -1/-1 counter = 1/1
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // First proliferate: add counter (now 2 -1/-1 counters = effective 0/0,
        // but SBA are not checked during ability resolution per MTG Rule 704.3)
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Second proliferate: bears is still on battlefield (SBA deferred),
        // choose nothing so it stays at 2 counters
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // After ability fully resolves, SBA kills bears (2/2 with 2 -1/-1 = 0/0)
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanTargetController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContagionEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two proliferate choices finish the ability and add every existing counter kind")
    void twoProliferationsFinishResolution() {
        Permanent engine = addReadyEngine(player1);
        engine.setCounterCount(CounterType.CHARGE, 1);
        engine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(engine.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(engine.getId(), player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(engine.getId(), player2.getId()));

        assertThat(engine.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(engine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with only energy counters can be chosen for both proliferations")
    void energyOnlyPlayerCanBeChosenTwice() {
        Permanent engine = addReadyEngine(player1);
        engine.setCounterCount(CounterType.CHARGE, 1);
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Proliferating without any counters finishes without a choice")
    void proliferateWithNoEligibleObjectsFinishes() {
        addReadyEngine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyEngine(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ContagionEngine());
        perm.setSummoningSick(false);
        return perm;
    }
}

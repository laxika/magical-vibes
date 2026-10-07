package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrullParasite.class, GrizzlyBears.class, Forest.class, DutifulThrull.class, TowerDefense.class})
class ThrullParasiteTest extends BaseCardTest {

    @BeforeEach
    void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    /** Adds a ready-to-tap Thrull Parasite (no summoning sickness) to player1's battlefield. */
    private void addReadyParasite() {
        harness.addToBattlefieldAndReturn(player1, new ThrullParasite()).setSummoningSick(false);
    }

    @Test
    @DisplayName("Tapping and paying 2 life removes a counter from target nonland permanent")
    void removesCounterAndPaysLife() {
        addReadyParasite();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A land is not a legal target")
    void cannotTargetLand() {
        addReadyParasite();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Extort drains each opponent when the payment is accepted")
    void extortDrainsOpponent() {
        harness.addToBattlefield(player1, new ThrullParasite());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Declining Extort leaves life totals unchanged")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new ThrullParasite());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A nonland permanent without counters is still a legal target")
    void canTargetPermanentWithoutCounters() {
        addReadyParasite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DutifulThrull());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dutiful Thrull");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller chooses a counter when the target has multiple counter types")
    void multipleCounterTypesRequireChoiceAtResolution() {
        addReadyParasite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DutifulThrull());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Thrull Parasite can remove a counter from itself")
    void canTargetItself() {
        Permanent parasite = harness.addToBattlefieldAndReturn(player1, new ThrullParasite());
        parasite.setSummoningSick(false);
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, parasite.getId());
        harness.passBothPriorities();

        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(parasite.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Summoning sickness prevents the tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent parasite = harness.addToBattlefieldAndReturn(player1, new ThrullParasite());
        parasite.setSummoningSick(true);
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, parasite.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(parasite.isTapped()).isFalse();
        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient life prevents activation without tapping the source")
    void cannotPayTwoLifeWithOneLife() {
        addReadyParasite();
        Permanent parasite = gd.playerBattlefields.get(player1.getId()).getFirst();
        parasite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, parasite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player1, 1);
        assertThat(parasite.isTapped()).isFalse();
        assertThat(parasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Extort waits for resolution and can be paid with white mana")
    void extortPaymentWaitsForResolutionAndAcceptsWhite() {
        harness.addToBattlefield(player1, new ThrullParasite());
        harness.setHand(player1, List.of(new DutifulThrull()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Extort")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new ThrullParasite());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DutifulThrull()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting Thrull Parasite does not trigger its own Extort")
    void castingParasiteDoesNotTriggerItsOwnExtort() {
        harness.setHand(player1, List.of(new ThrullParasite()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thrull Parasite");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting an instant triggers Extort and consumes only one black mana")
    void instantSpellTriggersExtort() {
        harness.addToBattlefield(player1, new ThrullParasite());
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}

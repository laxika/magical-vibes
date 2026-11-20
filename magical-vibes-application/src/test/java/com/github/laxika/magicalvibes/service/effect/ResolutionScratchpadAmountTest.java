package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * The amounts that read a resolving entry's scratchpad: "its power" / "its toughness" / "its mana value" after an
 * earlier step sacrificed a permanent, using its last-known information (CR 608.2h).
 */
class ResolutionScratchpadAmountTest {

    private AmountEvaluationService amountEvaluationService;
    private GameData gd;
    private StackEntry entry;
    private UUID controllerId;

    @BeforeEach
    void setUp() {
        amountEvaluationService = new AmountEvaluationService(
                mock(PredicateEvaluationService.class), mock(GameQueryService.class));
        controllerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", controllerId, "Player1");
        Card card = new Card();
        card.setName("Sacrifice then power");
        entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                List.of(new DrawCardEffect(1)));
    }

    private DepartureResult departure(Integer power, Integer toughness, boolean sacrificed) {
        return new DepartureResult(UUID.randomUUID(), Zone.GRAVEYARD, controllerId, UUID.randomUUID(),
                power, toughness, !sacrificed, sacrificed, controllerId, Set.of(), Set.of(), 2);
    }

    private int evaluate(com.github.laxika.magicalvibes.model.amount.DynamicAmount amount) {
        return amountEvaluationService.evaluate(gd, amount, AmountContext.forStackEntry(entry, null));
    }

    @Test
    @DisplayName("Power, toughness and mana value are the last-known values of what was sacrificed this way")
    void statsAreTheLastKnownValuesOfWhatWasSacrificed() {
        entry.recordDepartures(List.of(departure(5, 3, true)));

        assertThat(evaluate(AmountThisWay.sacrificedPower())).isEqualTo(5);
        assertThat(evaluate(AmountThisWay.sacrificedToughness())).isEqualTo(3);
        assertThat(evaluate(AmountThisWay.sacrificedManaValue())).isEqualTo(2);
    }

    @Test
    @DisplayName("Several sacrificed permanents add up; a negative power lowers the total, which is at least zero")
    void severalPermanentsAddUpAndNegativePowerLowersTheTotal() {
        entry.recordDepartures(List.of(departure(4, 4, true), departure(-2, 1, true), departure(null, null, true)));

        assertThat(evaluate(AmountThisWay.sacrificedPower())).isEqualTo(2);
        assertThat(evaluate(AmountThisWay.sacrificedToughness())).isEqualTo(5);

        entry.recordDepartures(List.of(departure(1, 1, true), departure(-3, 1, true)));

        assertThat(evaluate(AmountThisWay.sacrificedPower())).isZero();
    }

    @Test
    @DisplayName("A destroyed permanent is not counted as sacrificed")
    void destroyedPermanentIsNotCounted() {
        entry.recordDepartures(List.of(departure(4, 4, false)));

        assertThat(evaluate(AmountThisWay.sacrificedPower())).isZero();
    }

    @Test
    @DisplayName("Nothing sacrificed this way is zero")
    void nothingRemovedThisWayIsZero() {
        entry.recordDepartures(List.of());

        assertThat(evaluate(AmountThisWay.sacrificedPower())).isZero();
    }

    @Test
    @DisplayName("Only a sacrifice, destroy or exile step's last-known stats can be read")
    void statOfAnotherKindIsRefused() {
        assertThatThrownBy(() -> new AmountThisWay(StepKind.DRAW, AmountThisWay.Stat.POWER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A destroyed permanent exiled instead of put into a graveyard was destroyed but didn't die")
    void diedCountsOnlyWhatWentToAGraveyard() {
        entry.recordDestroyDepartures(List.of(destroyed(Zone.GRAVEYARD, controllerId),
                destroyed(Zone.EXILE, controllerId)));

        assertThat(evaluate(new AmountThisWay(StepKind.DESTROY))).isEqualTo(2);
        assertThat(evaluate(AmountThisWay.died(StepKind.DESTROY))).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the permanents the amount's controller controlled as they were destroyed count as theirs")
    void yoursCountsOnlyTheAmountControllersPermanents() {
        UUID opponentId = UUID.randomUUID();
        entry.recordDestroyDepartures(List.of(destroyed(Zone.GRAVEYARD, controllerId),
                destroyed(Zone.GRAVEYARD, opponentId), destroyed(Zone.GRAVEYARD, opponentId)));
        AmountThisWay yours = new AmountThisWay(StepKind.DESTROY).controlledByYou();

        assertThat(evaluate(yours)).isEqualTo(1);
        assertThat(amountEvaluationService.evaluate(gd, yours,
                AmountContext.forStackEntry(entry, null).withControllerId(opponentId))).isEqualTo(2);
    }

    @Test
    @DisplayName("Only a removal step records whether and whose what it removed died")
    void diedOfAnotherKindIsRefused() {
        assertThatThrownBy(() -> AmountThisWay.died(StepKind.DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AmountThisWay(StepKind.EXILE).controlledByYou())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A destroy step's mana value is the total last-known mana value of what it destroyed")
    void destroyedManaValueIsTheTotalOfWhatWasDestroyed() {
        entry.recordDestroyDepartures(List.of(destroyed(Zone.GRAVEYARD, controllerId),
                destroyed(Zone.EXILE, controllerId), stayed(controllerId)));

        assertThat(evaluate(AmountThisWay.destroyedManaValue())).isEqualTo(4);
    }

    @Test
    @DisplayName("Including what wasn't destroyed counts every permanent the destroy step applied to, by its controller")
    void includingUnremovedCountsWhatStayedToo() {
        UUID opponentId = UUID.randomUUID();
        entry.recordDestroyDepartures(List.of(destroyed(Zone.GRAVEYARD, controllerId), stayed(opponentId),
                destroyed(Zone.EXILE, opponentId)));
        AmountThisWay applied = new AmountThisWay(StepKind.DESTROY).includingUnremoved();

        assertThat(evaluate(applied)).isEqualTo(3);
        assertThat(evaluate(new AmountThisWay(StepKind.DESTROY))).isEqualTo(2);
        assertThat(amountEvaluationService.evaluate(gd, applied.controlledByYou(),
                AmountContext.forStackEntry(entry, null).withControllerId(opponentId))).isEqualTo(2);
    }

    @Test
    @DisplayName("Including what wasn't destroyed reads nothing when a later destroy step recorded no departures")
    void includingUnremovedIgnoresAnOlderStepsDepartures() {
        entry.recordDestroyDepartures(List.of(stayed(controllerId)));
        entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(List.of(UUID.randomUUID()), List.of(controllerId)));

        assertThat(evaluate(new AmountThisWay(StepKind.DESTROY).includingUnremoved())).isZero();
    }

    @Test
    @DisplayName("Only a destroy step's count covers what it didn't remove, and not as deaths")
    void includingUnremovedOfAnotherKindIsRefused() {
        assertThatThrownBy(() -> new AmountThisWay(StepKind.SACRIFICE).includingUnremoved())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AmountThisWay.died(StepKind.DESTROY).includingUnremoved())
                .isInstanceOf(IllegalArgumentException.class);
    }

    private DepartureResult stayed(UUID controller) {
        return new DepartureResult(UUID.randomUUID(), Zone.BATTLEFIELD, null, null, 2, 2, false, false, controller,
                Set.of(), Set.of(), 2);
    }

    private DepartureResult destroyed(Zone destination, UUID lastKnownControllerId) {
        return new DepartureResult(UUID.randomUUID(), destination, lastKnownControllerId, UUID.randomUUID(),
                2, 2, true, false, lastKnownControllerId, Set.of(), Set.of(), 2);
    }

    @Test
    @DisplayName("A conditional amount reads the scratchpad")
    void conditionalAmountReadsTheScratchpad() {
        FixedIfCondition amount = new FixedIfCondition(HappenedThisWay.sacrificed(), 2, 0);
        assertThat(evaluate(amount)).isZero();

        entry.recordDepartures(List.of(departure(1, 1, true)));

        assertThat(evaluate(amount)).isEqualTo(2);
    }
}

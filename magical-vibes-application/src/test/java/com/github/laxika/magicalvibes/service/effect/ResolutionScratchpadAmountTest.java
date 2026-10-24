package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.PermanentLeftThisWayPower;
import com.github.laxika.magicalvibes.model.amount.PermanentLeftThisWayToughness;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * The amounts that read a resolving entry's scratchpad: "its power" / "its toughness" after an
 * earlier step removed a permanent, using its last-known information (CR 608.2h).
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
                power, toughness, false, sacrificed, controllerId);
    }

    private int evaluate(com.github.laxika.magicalvibes.model.amount.DynamicAmount amount) {
        return amountEvaluationService.evaluate(gd, amount, AmountContext.forStackEntry(entry, null));
    }

    @Test
    void powerAndToughnessAreTheLastKnownValuesOfWhatLeftThisWay() {
        entry.recordDepartures(List.of(departure(5, 3, true)));

        assertThat(evaluate(new PermanentLeftThisWayPower())).isEqualTo(5);
        assertThat(evaluate(new PermanentLeftThisWayToughness())).isEqualTo(3);
    }

    @Test
    void severalPermanentsAddUpAndNegativePowerCountsAsZero() {
        entry.recordDepartures(List.of(departure(4, 4, true), departure(-2, 1, true), departure(null, null, true)));

        assertThat(evaluate(new PermanentLeftThisWayPower())).isEqualTo(4);
        assertThat(evaluate(new PermanentLeftThisWayToughness())).isEqualTo(5);
    }

    @Test
    void nothingRemovedThisWayIsZero() {
        entry.recordDepartures(List.of());

        assertThat(evaluate(new PermanentLeftThisWayPower())).isZero();
    }

    @Test
    void conditionalAmountReadsTheScratchpad() {
        FixedIfCondition amount = new FixedIfCondition(PermanentLeftThisWay.sacrificed(), 2, 0);
        assertThat(evaluate(amount)).isZero();

        entry.recordDepartures(List.of(departure(1, 1, true)));

        assertThat(evaluate(amount)).isEqualTo(2);
    }
}

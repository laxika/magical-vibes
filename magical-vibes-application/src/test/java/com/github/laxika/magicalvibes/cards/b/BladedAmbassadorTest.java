package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BladedAmbassadorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an oil counter")
    void entersWithOilCounter() {
        harness.setHand(player1, List.of(new BladedAmbassador()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ambassador = findPermanent(player1, "Bladed Ambassador");
        assertThat(ambassador.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing an oil counter grants indestructible until end of turn")
    void removesOilCounterAndGrantsIndestructible() {
        Permanent ambassador = addAmbassadorReady(player1);
        ambassador.setCounterCount(CounterType.OIL, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ambassador.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, ambassador, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an oil counter")
    void cannotActivateWithoutOilCounter() {
        addAmbassadorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent ambassador = addAmbassadorReady(player1);
        ambassador.setCounterCount(CounterType.OIL, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ambassador, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ambassador, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addAmbassadorReady(Player player) {
        return addCreatureReady(player, new BladedAmbassador());
    }
}

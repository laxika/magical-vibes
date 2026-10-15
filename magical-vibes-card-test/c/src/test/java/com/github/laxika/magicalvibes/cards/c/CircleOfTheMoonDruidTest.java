package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfTheMoonDruid.class})
class CircleOfTheMoonDruidTest extends BaseCardTest {

    @Test
    @DisplayName("During its controller's turn, Circle of the Moon Druid is a 4/2 Bear")
    void bearFormDuringControllerTurn() {
        Permanent druid = addDruid(player1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, druid))
                .containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Outside its controller's turn, Circle of the Moon Druid has its printed characteristics")
    void printedCharacteristicsDuringOpponentsTurn() {
        Permanent druid = addDruid(player1);
        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, druid))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ELF, CardSubtype.DRUID);
    }

    @Test
    @DisplayName("Bear Form follows each Druid's controller and updates when the turn changes")
    void bearFormTracksBothControllersAcrossTurns() {
        Permanent firstDruid = addDruid(player1);
        Permanent secondDruid = addDruid(player2);
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, firstDruid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstDruid)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, firstDruid)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, secondDruid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondDruid)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, secondDruid))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ELF, CardSubtype.DRUID);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, firstDruid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstDruid)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, firstDruid))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ELF, CardSubtype.DRUID);
        assertThat(gqs.getEffectivePower(gd, secondDruid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondDruid)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, secondDruid)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of Bear Form's base power and toughness")
    void countersApplyInBothForms() {
        Permanent druid = addDruid(player1);
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(4);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(6);
    }
    private Permanent addDruid(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CircleOfTheMoonDruid());
    }
}

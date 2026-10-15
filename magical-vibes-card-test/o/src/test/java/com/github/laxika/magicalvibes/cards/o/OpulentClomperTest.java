package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpulentClomper.class, SuntailHawk.class, CloudSprite.class})
class OpulentClomperTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of colors among permanents you control")
    void powerAndToughnessCountControlledColors() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());
        assertThat(gqs.getEffectivePower(gd, clomper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clomper)).isEqualTo(1);

        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, clomper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, clomper)).isEqualTo(3);
    }

    @Test
    @DisplayName("At upkeep, it permanently gains one randomly selected missing color")
    void upkeepAddsOneMissingColor() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());
        Set<CardColor> before = gqs.getEffectiveColors(gd, clomper);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Set<CardColor> after = gqs.getEffectiveColors(gd, clomper);
        assertThat(after).containsAll(before);
        assertThat(after).hasSize(before.size() + 1);
        assertThat(after).containsAnyOf(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED);
    }

    @Test
    @DisplayName("Does not trigger once it already has all five colors")
    void doesNotTriggerWhenAllColorsArePresent() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectiveColors(gd, clomper)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, clomper)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Counts each color once and ignores opponents' permanents and cards outside the battlefield")
    void countsOnlyDistinctColorsOfControlledPermanents() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.setGraveyard(player1, List.of(new CloudSprite()));

        assertThat(gqs.getEffectivePower(gd, clomper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clomper)).isEqualTo(2);

        harness.addToBattlefield(player1, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, clomper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, clomper)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's upkeep does not trigger a color gain")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, clomper)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Colors gained on successive upkeeps increase its power and toughness")
    void gainedColorsIncreasePowerAndToughness() {
        Permanent clomper = addCreatureReady(player1, new OpulentClomper());

        for (int expectedSize = 2; expectedSize <= 5; expectedSize++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gqs.getEffectiveColors(gd, clomper)).hasSize(expectedSize);
            assertThat(gqs.getEffectivePower(gd, clomper)).isEqualTo(expectedSize);
            assertThat(gqs.getEffectiveToughness(gd, clomper)).isEqualTo(expectedSize);
        }
    }
}

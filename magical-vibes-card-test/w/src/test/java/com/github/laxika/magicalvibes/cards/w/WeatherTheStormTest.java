package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.StackEntry;




@CardUsed({WeatherTheStorm.class, GrizzlyBears.class})
class WeatherTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life")
    void gainsLife() {
        harness.setLife(player1, 10);
        castWeatherTheStorm();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Storm gains 3 life for each copy")
    void stormCopiesGainLife() {
        harness.setLife(player1, 10);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castWeatherTheStorm();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A later opposing spell does not increase the earlier storm count")
    void stormCountIsFixedWhenCast() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castWeatherTheStorm();
        harness.setHand(player2, List.of(new WeatherTheStorm()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 26);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 26);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm copies are not casts and do not increase subsequent storm counts")
    void stormCopiesDoNotCountAsCasts() {
        harness.setLife(player1, 20);
        for (int spellNumber = 1; spellNumber <= 3; spellNumber++) {
            castWeatherTheStorm();
            harness.passBothPriorities();
            assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(spellNumber - 1);
            for (int spell = 0; spell < spellNumber; spell++) {
                harness.passBothPriorities();
            }
            assertThat(gd.stack).isEmpty();
            harness.assertLife(player1, 20 + 3 * spellNumber * (spellNumber + 1) / 2);
            harness.assertLife(player2, 20);
        }
    }

    private void castWeatherTheStorm() {
        harness.setHand(player1, List.of(new WeatherTheStorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }
}

@CardUsed({WeatherTheStorm.class, GrizzlyBears.class})
class Mh1WeatherTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 3 life")
    void gainsThreeLife() {
        harness.setLife(player1, 10);
        castWeatherTheStorm();

        resolveStormAndSpell();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Weather the Storm")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setLife(player1, 20);
        castWeatherTheStorm();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(29);
    }

    private void castWeatherTheStorm() {
        harness.setHand(player1, List.of(new WeatherTheStorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }

    private void resolveStormAndSpell() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

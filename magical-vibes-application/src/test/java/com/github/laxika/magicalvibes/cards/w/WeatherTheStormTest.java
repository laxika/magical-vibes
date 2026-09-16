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

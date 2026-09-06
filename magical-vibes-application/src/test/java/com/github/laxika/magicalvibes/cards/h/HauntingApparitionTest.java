package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Brushwagg;
import com.github.laxika.magicalvibes.cards.e.EarlyHarvest;
import com.github.laxika.magicalvibes.cards.m.MerfolkRaiders;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingApparition.class, Brushwagg.class, EarlyHarvest.class, MerfolkRaiders.class})
class HauntingApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Power is 1 with empty graveyards; toughness stays 2")
    void powerIsOneWithEmptyGraveyards() {
        Permanent apparition = addCreatureReady(player1, new HauntingApparition());

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power is 1 plus green creature cards in the chosen opponent's graveyard")
    void powerCountsOpponentGreenCreatures() {
        Permanent apparition = addCreatureReady(player1, new HauntingApparition());
        harness.setGraveyard(player2, List.of(new Brushwagg(), new Brushwagg()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green noncreature cards and nongreen creature cards do not count")
    void onlyGreenCreatureCardsCount() {
        Permanent apparition = addCreatureReady(player1, new HauntingApparition());
        harness.setGraveyard(player2, List.of(new Brushwagg(), new EarlyHarvest(), new MerfolkRaiders()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green creature cards in the controller's own graveyard do not count")
    void ownGraveyardDoesNotCount() {
        Permanent apparition = addCreatureReady(player1, new HauntingApparition());
        harness.setGraveyard(player1, List.of(new Brushwagg(), new Brushwagg()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as green creature cards enter the opponent's graveyard")
    void powerUpdatesDynamically() {
        Permanent apparition = addCreatureReady(player1, new HauntingApparition());
        harness.setGraveyard(player2, List.of(new Brushwagg()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new Brushwagg(), new Brushwagg()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(3);
    }
}

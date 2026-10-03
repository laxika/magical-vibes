package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathsShadow.class})
class DeathsShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -X/-X based on its controller's life total")
    void getsMinusLifeTotal() {
        gd.playerLifeTotals.put(player1.getId(), 8);
        Permanent shadow = addCreatureReady(player1, new DeathsShadow());

        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates as its controller's life total changes")
    void updatesWithLifeTotal() {
        gd.playerLifeTotals.put(player1.getId(), 8);
        Permanent shadow = addCreatureReady(player1, new DeathsShadow());

        gd.playerLifeTotals.put(player1.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(10);
    }

    @Test
    @DisplayName("Shrinks when its controller gains life")
    void shrinksWithLifeGain() {
        harness.setLife(player1, 3);
        Permanent shadow = addCreatureReady(player1, new DeathsShadow());

        harness.setLife(player1, 12);

        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Shadow uses only its own controller's life total")
    void usesOwnControllerLifeTotal() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 3);
        Permanent first = addCreatureReady(player1, new DeathsShadow());
        Permanent second = addCreatureReady(player2, new DeathsShadow());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(10);
    }

    @Test
    @DisplayName("Resolves and survives at twelve life as a 1/1")
    void survivesAtTwelveLife() {
        harness.setLife(player1, 12);
        harness.setHand(player1, List.of(new DeathsShadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shadow = findPermanent(player1, "Death's Shadow");
        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Death's Shadow");
    }

    @ParameterizedTest
    @ValueSource(ints = {13, 20})
    @DisplayName("Dies after resolving when its toughness is zero or less")
    void diesAtThirteenOrMoreLife(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new DeathsShadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Death's Shadow");
        harness.assertInGraveyard(player1, "Death's Shadow");
    }

    @Test
    @DisplayName("Dies when a life increase reduces its toughness to zero")
    void diesAfterLifeIncrease() {
        harness.setLife(player1, 12);
        addCreatureReady(player1, new DeathsShadow());

        harness.setLife(player1, 13);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Death's Shadow");
        harness.assertInGraveyard(player1, "Death's Shadow");
    }
}

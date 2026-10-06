package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuhamDjinn.class, Forest.class, GoblinSpy.class, KavuLair.class,
        MetathranZombie.class, YavimayaBarbarian.class})
class RuhamDjinnTest extends BaseCardTest {

    private Permanent addRuhamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new RuhamDjinn());
    }

    @Test
    @DisplayName("Shrinks when white is the most common color")
    void shrinksWhenWhiteIsMostCommon() {
        Permanent ruham = addRuhamDjinn();

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when white is tied for most common color")
    void shrinksWhenWhiteIsTied() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2,
                new YavimayaBarbarian());
        harness.addToBattlefield(player2,
                new GoblinSpy());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new KavuLair());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Reevaluates the penalty as the most common color changes")
    void reevaluatesPenaltyAsPermanentsEnter() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);

        Permanent secondDjinn = harness.addToBattlefieldAndReturn(player2, new RuhamDjinn());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondDjinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondDjinn)).isEqualTo(3);

        harness.addToBattlefield(player1, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondDjinn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondDjinn)).isEqualTo(5);
    }

    @Test
    @DisplayName("Colored enchantments can make another color strictly more common")
    void noncreaturesCanRemovePenalty() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player1, new KavuLair());
        harness.addToBattlefield(player2, new KavuLair());

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cards in hands and graveyards do not affect permanent color counts")
    void ignoresCardsOutsideBattlefield() {
        Permanent ruham = addRuhamDjinn();
        harness.setHand(player2, List.of(new MetathranZombie(), new MetathranZombie()));
        harness.setGraveyard(player1, List.of(new MetathranZombie(), new MetathranZombie()));

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("First strike kills a blocker before it damages Ruham Djinn")
    void firstStrikePreventsBlockerDamage() {
        Permanent ruham = addCreatureReady(player1, new RuhamDjinn());
        ruham.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Ruham Djinn");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        assertThat(ruham.getMarkedDamage()).isZero();
    }
}

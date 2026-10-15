package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeticulousArtisan.class, GrizzlyBears.class, Shock.class})
class MeticulousArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("When Meticulous Artisan enters, it creates a Treasure token")
    void entersWithTreasureToken() {
        harness.castFromHand(player1, new MeticulousArtisan(), "{3}{R}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Casting a noncreature spell triggers prowess")
    void noncreatureSpellPumps() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Prowess wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Prowess resolves before the spell and accumulates for repeated casts")
    void repeatedCastsAccumulateProwess() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 4);
    }

    @Test
    @DisplayName("Entering without being cast still creates an untapped Treasure for the controller")
    void noncastEntryCreatesTreasure() {
        harness.enterBattlefieldAndReturn(player2, new MeticulousArtisan());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanent(player2, "Treasure").isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private Permanent addArtisan() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new MeticulousArtisan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return artisan;
    }
}

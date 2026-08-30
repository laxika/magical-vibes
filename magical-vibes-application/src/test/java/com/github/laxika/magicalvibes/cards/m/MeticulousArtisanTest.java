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
        harness.setHand(player1, List.of(new MeticulousArtisan()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent artisan = addArtisan();
        int initialPower = gqs.getEffectivePower(gd, artisan);
        int initialToughness = gqs.getEffectiveToughness(gd, artisan);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(initialToughness);
    }

    private Permanent addArtisan() {
        harness.addToBattlefield(player1, new MeticulousArtisan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

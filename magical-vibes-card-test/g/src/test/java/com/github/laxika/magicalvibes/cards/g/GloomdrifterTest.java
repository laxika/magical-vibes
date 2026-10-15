package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gloomdrifter.class, DrudgeSkeletons.class, GrizzlyBears.class, HillGiant.class})
class GloomdrifterTest extends BaseCardTest {

    @Test
    @DisplayName("With threshold, its ETB trigger gives nonblack creatures -2/-2")
    void thresholdTriggersOnEntering() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifter();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Gloomdrifter");
    }

    @Test
    @DisplayName("Without threshold, its ETB trigger does not occur")
    void doesNotTriggerBelowThreshold() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Once triggered, its ETB ability resolves even if threshold is lost")
    void thresholdTriggerStillResolvesAfterThresholdIsLost() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifterSpell();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Its -2/-2 effect lasts only until end of turn")
    void effectExpiresAtEndOfTurn() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifter();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        addCreatures();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castGloomdrifter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Gaining threshold after entering does not trigger the ability")
    void gainingThresholdAfterEnteringDoesNotTrigger() {
        addCreatures();
        harness.setGraveyard(player1, List.of());
        castGloomdrifterSpell();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability affects creatures present at resolution, not later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifterSpell();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new DrudgeSkeletons());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        harness.addToBattlefield(player2, new HillGiant());
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Hill Giant"))).isEqualTo(3);
    }

    private void addCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player2, new HillGiant());
    }

    private void castGloomdrifter() {
        castGloomdrifterSpell();
        harness.passBothPriorities();
    }

    private void castGloomdrifterSpell() {
        harness.setHand(player1, List.of(new Gloomdrifter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

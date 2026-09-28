package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobobrainWarMind.class, CopperMyr.class, GrizzlyBears.class})
class RobobrainWarMindTest extends BaseCardTest {

    @Test
    void powerTracksControllerHandAndToughnessIsFive() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, robobrain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robobrain)).isEqualTo(5);
    }

    @Test
    void entersWithEnergyForEachArtifactCreatureControlled() {
        addCreatureReady(player1, new CopperMyr());
        harness.setHand(player1, List.of(new RobobrainWarMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayThreeEnergyToDrawOnAttack() {
        addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPayWithoutThreeEnergy() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, robobrain)).isZero();
    }

    @Test
    void decliningPaymentDoesNotDraw() {
        addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

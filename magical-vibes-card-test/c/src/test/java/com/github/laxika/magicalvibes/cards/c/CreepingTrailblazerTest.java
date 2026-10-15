package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepingTrailblazer.class, AirElemental.class, GreenwoodSentinel.class})
class CreepingTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elementals you control get +1/+0")
    void buffsOtherElementalsYouControl() {
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent nonElemental = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent opponentElemental = addCreatureReady(player2, new AirElemental());
        int elementalPower = gqs.getEffectivePower(gd, elemental);
        int elementalToughness = gqs.getEffectiveToughness(gd, elemental);
        int nonElementalPower = gqs.getEffectivePower(gd, nonElemental);
        int opponentElementalPower = gqs.getEffectivePower(gd, opponentElemental);

        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(elementalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(elementalToughness);
        assertThat(gqs.getEffectivePower(gd, nonElemental)).isEqualTo(nonElementalPower);
        assertThat(gqs.getEffectivePower(gd, opponentElemental)).isEqualTo(opponentElementalPower);
        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability counts Elementals you control and wears off at cleanup")
    void activatedAbilityCountsElementalsAndExpires() {
        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player2, new AirElemental());
        int initialPower = gqs.getEffectivePower(gd, trailblazer);
        int initialToughness = gqs.getEffectiveToughness(gd, trailblazer);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Elementals entering before resolution count, but later arrivals do not increase the boost")
    void countsElementalsAtResolutionAndKeepsTheResolvedAmount() {
        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());
        int initialPower = gqs.getEffectivePower(gd, trailblazer);
        int initialToughness = gqs.getEffectiveToughness(gd, trailblazer);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        addCreatureReady(player1, new AirElemental());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);

        addCreatureReady(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);
    }

    @Test
    @DisplayName("An Elemental leaving before resolution is not counted, and leaving afterward does not reduce the boost")
    void departingElementalsOnlyAffectUnresolvedAbilities() {
        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());
        Permanent firstElemental = addCreatureReady(player1, new AirElemental());
        Permanent secondElemental = addCreatureReady(player1, new AirElemental());
        int initialPower = gqs.getEffectivePower(gd, trailblazer);
        int initialToughness = gqs.getEffectiveToughness(gd, trailblazer);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(firstElemental);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);

        gd.playerBattlefields.get(player1.getId()).remove(secondElemental);

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);
    }

    @Test
    @DisplayName("Trailblazers boost each other and repeated activations accumulate only on their source")
    void multipleTrailblazersAndRepeatedActivations() {
        Permanent first = addCreatureReady(player1, new CreepingTrailblazer());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);
        Permanent second = addCreatureReady(player1, new CreepingTrailblazer());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness);
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninLightscribe.class, BarkshellBlessing.class, GiantGrowth.class,
        GrizzlyBears.class, ExpandedAnatomy.class})
class LeoninLightscribeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts all creatures you control until end of turn")
    void castingInstantBoostsOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant triggers Leonin Lightscribe")
    void copyingInstantBoostsOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Leonin Lightscribe")
    void castingCreatureDoesNotBoostOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(lightscribe.getEffectivePower()).isEqualTo(2);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(lightscribe.getEffectivePower()).isEqualTo(2);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a sorcery boosts creatures before the sorcery resolves")
    void castingSorceryBoostsOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, lightscribe.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(3);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentCastingInstantDoesNotBoostOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Magecraft affects creatures present when it resolves, including new arrivals")
    void boostUsesCreaturesPresentAtResolution() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, lightscribe.getId());
        Permanent earlyArrival = addCreatureReady(player1, new LeoninLightscribe());
        harness.passBothPriorities();
        Permanent lateArrival = addCreatureReady(player1, new LeoninLightscribe());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightscribe)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lightscribe)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, earlyArrival)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, earlyArrival)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateArrival)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateArrival)).isEqualTo(2);
    }
}

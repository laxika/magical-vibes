package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MistfireAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell pumps Mistfire Adept and grants flying to a chosen creature")
    void noncreatureSpellPumpsAndGrantsFlying() {
        harness.addToBattlefield(player1, new MistfireAdept());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent adept = findPermanent(player1, "Mistfire Adept");
        int initialPower = gqs.getEffectivePower(gd, adept);
        int initialToughness = gqs.getEffectiveToughness(gd, adept);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(initialToughness + 1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger either ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MistfireAdept());
        Permanent adept = findPermanent(player1, "Mistfire Adept");
        int initialPower = gqs.getEffectivePower(gd, adept);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("The prowess boost and granted flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MistfireAdept());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent adept = findPermanent(player1, "Mistfire Adept");
        int initialPower = gqs.getEffectivePower(gd, adept);
        int initialToughness = gqs.getEffectiveToughness(gd, adept);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(initialToughness + 1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(initialPower);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}

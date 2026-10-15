package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonMantle.class, GrizzlyBears.class, FountainOfYouth.class})
class KeldonMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Black ability gives the enchanted creature a regeneration shield")
    void regeneratesEnchantedCreature() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Red ability gives the enchanted creature +1/+0 until end of turn")
    void boostsEnchantedCreatureUntilEndOfTurn() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green ability gives the enchanted creature trample until end of turn")
    void grantsTrampleUntilEndOfTurn() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Green ability still affects the creature if Keldon Mantle leaves before resolution")
    void greenAbilityUsesEnchantedCreatureCapturedAtActivation() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KeldonMantle());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 2, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Keldon Mantle cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KeldonMantle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Black ability still regenerates the creature after the Aura leaves")
    void regeneratesAfterAuraLeaves() {
        Permanent bears = addEnchantedBears();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Red ability still boosts the creature after the Aura leaves")
    void boostsAfterAuraLeaves() {
        Permanent bears = addEnchantedBears();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated red activations stack and expire together")
    void repeatedBoostsStack() {
        Permanent bears = addEnchantedBears();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Aura controller can activate every ability on an opponent's enchanted creature")
    void enchantsAndAffectsOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KeldonMantle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getAttachedTo()).isEqualTo(bears.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addEnchantedBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KeldonMantle());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}

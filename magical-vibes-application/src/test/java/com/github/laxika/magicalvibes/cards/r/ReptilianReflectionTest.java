package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReptilianReflection.class, Censor.class, GrizzlyBears.class})
class ReptilianReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card offers to animate Reptilian Reflection")
    void cyclingOffersAnimation() {
        addReflection();

        cycleCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the cycling trigger makes Reptilian Reflection a 5/4 Dinosaur with trample and haste")
    void acceptsAnimation() {
        Permanent reflection = addReflection();

        cycleCard();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, reflection)).isTrue();
        assertThat(gqs.isCreature(gd, reflection)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, reflection)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, reflection)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reflection)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent reflection = addReflection();

        cycleCard();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, reflection)).isTrue();
        assertThat(gqs.isCreature(gd, reflection)).isFalse();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining the cycling trigger leaves Reptilian Reflection unchanged")
    void declinesAnimation() {
        Permanent reflection = addReflection();

        cycleCard();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isEnchantment(gd, reflection)).isTrue();
        assertThat(gqs.isCreature(gd, reflection)).isFalse();
    }

    private Permanent addReflection() {
        return harness.addToBattlefieldAndReturn(player1, new ReptilianReflection());
    }

    private void cycleCard() {
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.s.StartlingDevelopment;
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

@CardUsed({ReptilianReflection.class, StartlingDevelopment.class, CatharticReunion.class})
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

    @Test
    @DisplayName("An opponent cycling a card does not animate your Reflection")
    void opponentCyclingDoesNotTrigger() {
        Permanent reflection = addReflection();
        harness.setHand(player2, List.of(new StartlingDevelopment()));
        harness.setLibrary(player2, List.of(new ReptilianReflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, reflection)).isFalse();
        harness.assertInHand(player2, "Reptilian Reflection");
    }

    @Test
    @DisplayName("Animating your Reflection leaves an opponent's Reflection unchanged")
    void onlySourceReflectionIsAnimated() {
        Permanent reflection = addReflection();
        Permanent opposingReflection = harness.addToBattlefieldAndReturn(player2, new ReptilianReflection());

        cycleCard();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, reflection)).isTrue();
        assertThat(gqs.isCreature(gd, opposingReflection)).isFalse();
    }

    @Test
    @DisplayName("Two Reflections offer independent animation choices")
    void multipleReflectionsHaveIndependentChoices() {
        Permanent first = addReflection();
        Permanent second = addReflection();

        cycleCard();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(List.of(first, second).stream().filter(p -> gqs.isCreature(gd, p)).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining a second cycling trigger does not undo an earlier animation")
    void decliningSecondTriggerKeepsAnimation() {
        Permanent reflection = addReflection();
        cycleCard();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        cycleCard();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, reflection)).isTrue();
        assertThat(gqs.getEffectivePower(gd, reflection)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reflection)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Discarding cycling cards as a spell's cost does not trigger Reflection")
    void ordinaryDiscardDoesNotTrigger() {
        Permanent reflection = addReflection();
        harness.setHand(player1, List.of(new CatharticReunion(),
                new StartlingDevelopment(), new StartlingDevelopment()));
        harness.setLibrary(player1, List.of(new ReptilianReflection(),
                new ReptilianReflection(), new ReptilianReflection()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, (java.util.UUID) null, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, reflection)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private Permanent addReflection() {
        return harness.addToBattlefieldAndReturn(player1, new ReptilianReflection());
    }

    private void cycleCard() {
        harness.setHand(player1, List.of(new StartlingDevelopment()));
        harness.setLibrary(player1, List.of(new ReptilianReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }
}

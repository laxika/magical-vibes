package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformShrieker.class, Conspiracy.class})
class MistformShriekerTest extends BaseCardTest {

    @Test
    void activatingPromptsForCreatureTypeWithoutRequiringATarget() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(shrieker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    void chosenCreatureTypeReplacesOldTypeUntilEndOfTurn() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void wallIsALegalCreatureTypeChoice() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.WALL);
    }

    @Test
    void chosenCreatureTypeWearsOffAtEndOfTurn() {
        Permanent shrieker = addReadyShrieker();
        var originalSubtypes = gqs.effectiveCreatureSubtypes(gd, shrieker);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactlyElementsOf(originalSubtypes);
    }

    @Test
    void secondActivationReplacesTheFirstChosenCreatureType() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChoose(CardSubtype.GOBLIN);
        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.WALL);
    }

    @Test
    void abilityCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, new MistformShrieker());
        shrieker.setSummoningSick(true);
        shrieker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(shrieker.isTapped()).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void abilityCannotBeActivatedWithoutMana() {
        Permanent shrieker = addReadyShrieker();
        var originalSubtypes = gqs.effectiveCreatureSubtypes(gd, shrieker);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactlyElementsOf(originalSubtypes);
    }

    @Test
    void typeChoiceIsMadeOnResolutionRatherThanActivation() {
        Permanent shrieker = addReadyShrieker();
        var originalSubtypes = gqs.effectiveCreatureSubtypes(gd, shrieker);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactlyElementsOf(originalSubtypes);

        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void turningFaceUpRequiresTwoBlueMana() {
        harness.setHand(player1, List.of(new MistformShrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent shrieker = findPermanent(player1, "Mistform Shrieker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shrieker.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    void typeChangingAbilityIsAvailableAfterTurningFaceUp() {
        harness.setHand(player1, List.of(new MistformShrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent shrieker = findPermanent(player1, "Mistform Shrieker");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(shrieker.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new MistformShrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shrieker = findPermanent(player1, "Mistform Shrieker");
        assertThat(shrieker.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shrieker));
        harness.passBothPriorities();

        assertThat(shrieker.isFaceDown()).isFalse();
    }

    private Permanent addReadyShrieker() {
        return addCreatureReady(player1, new MistformShrieker());
    }

    @Test
    void laterConspiracyOverridesEarlierActivatedTypeChange() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activateAndChoose(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.ELF);
    }

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}

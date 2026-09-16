package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MistformShrieker.class)
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
        harness.clearPriorityPassed();
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
    void morphsFaceDownAndCanBeTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new MistformShrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
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

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}

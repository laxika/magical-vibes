package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistformDreamer.class, Conspiracy.class})
class MistformDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a creature type without requiring a target")
    void activatingPromptsForCreatureType() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(dreamer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    @DisplayName("The chosen creature type replaces the old type until end of turn")
    void chosenCreatureTypeReplacesOldType() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Wall is a legal creature type choice")
    void wallCanBeChosen() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenCreatureTypeWearsOff() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("A second activation replaces the first chosen creature type")
    void secondActivationReplacesFirstChosenType() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChoose(CardSubtype.GOBLIN);
        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Dreamer can change its creature type")
    void tappedSummoningSickDreamerCanActivate() {
        Permanent dreamer = harness.addToBattlefieldAndReturn(player1, new MistformDreamer());
        dreamer.setSummoningSick(true);
        dreamer.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.GOBLIN);
        assertThat(dreamer.isTapped()).isTrue();
        assertThat(dreamer.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Changing Dreamer's type does not change other creatures' types")
    void changingTypeAffectsOnlySource() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        Permanent otherDreamer = addCreatureReady(player1, new MistformDreamer());
        Permanent opposingDreamer = addCreatureReady(player2, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, otherDreamer)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingDreamer)).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("A later Conspiracy replaces Dreamer's previously chosen creature type")
    void laterConspiracyReplacesChosenType() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activateAndChoose(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Dreamer's later activation replaces an earlier Conspiracy type")
    void laterActivationReplacesConspiracyType() {
        Permanent dreamer = addCreatureReady(player1, new MistformDreamer());
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.GOBLIN);
    }

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}

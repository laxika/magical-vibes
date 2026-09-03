package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MistformDreamer.class)
class MistformDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a creature type without requiring a target")
    void activatingPromptsForCreatureType() {
        addReadyDreamer();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    @DisplayName("The chosen creature type replaces the old type until end of turn")
    void chosenCreatureTypeReplacesOldType() {
        Permanent dreamer = addReadyDreamer();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Wall is a legal creature type choice")
    void wallCanBeChosen() {
        Permanent dreamer = addReadyDreamer();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dreamer)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenCreatureTypeWearsOff() {
        Permanent dreamer = addReadyDreamer();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dreamer.getTransientCreatureTypeOverride()).isNull();
    }

    private Permanent addReadyDreamer() {
        Permanent dreamer = new Permanent(new MistformDreamer());
        dreamer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dreamer);
        return dreamer;
    }

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}

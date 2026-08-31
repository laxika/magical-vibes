package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MistformMask.class, GrizzlyBears.class})
class MistformMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the Aura ability prompts for a creature type")
    void activatingPromptsForCreatureType() {
        addAttachedMask();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    @DisplayName("The enchanted creature becomes the chosen type until end of turn")
    void enchantedCreatureBecomesChosenType() {
        Permanent creature = addAttachedMask();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenCreatureTypeWearsOff() {
        Permanent creature = addAttachedMask();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.BEAR);
    }

    private Permanent addAttachedMask() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MistformMask());
        mask.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        return creature;
    }
}

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

@CardUsed(MistformSeaswift.class)
class MistformSeaswiftTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a creature type without requiring a target")
    void activatingPromptsForCreatureType() {
        addReadySeaswift();
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
        Permanent seaswift = addReadySeaswift();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, seaswift)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenCreatureTypeWearsOff() {
        Permanent seaswift = addReadySeaswift();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(seaswift.getTransientCreatureTypeOverride()).isNull();
    }

    private Permanent addReadySeaswift() {
        Permanent seaswift = new Permanent(new MistformSeaswift());
        seaswift.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seaswift);
        return seaswift;
    }

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MistformShrieker.class)
class MistformShriekerTest extends BaseCardTest {

    @Test
    void activatingPromptsForCreatureTypeWithoutRequiringATarget() {
        addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    void chosenCreatureTypeReplacesOldTypeUntilEndOfTurn() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, shrieker)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    void chosenCreatureTypeWearsOffAtEndOfTurn() {
        Permanent shrieker = addReadyShrieker();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrieker.getTransientCreatureTypeOverride()).isNull();
    }

    private Permanent addReadyShrieker() {
        Permanent shrieker = new Permanent(new MistformShrieker());
        shrieker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shrieker);
        return shrieker;
    }
}

package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Imagecrafter.class, GrizzlyBears.class})
class ImagecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen type until end of turn")
    void targetBecomesChosenType() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent imagecrafter = addImagecrafter();

        activate(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardSubtype.WALL.name());

        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(imagecrafter.isTapped()).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Wall is not a legal creature type choice")
    void wallCannotBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addImagecrafter();

        activate(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, CardSubtype.WALL.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid creature type choice");
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenTypeWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addImagecrafter();

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
    }

    private Permanent addImagecrafter() {
        Permanent imagecrafter = harness.addToBattlefieldAndReturn(player1, new Imagecrafter());
        imagecrafter.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        return imagecrafter;
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
    }
}

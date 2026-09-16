package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({Imagecrafter.class, GlorySeeker.class, Island.class})
class ImagecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen type until end of turn")
    void targetBecomesChosenType() {
        Permanent imagecrafter = addImagecrafter();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

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
        addImagecrafter();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        activate(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, CardSubtype.WALL.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid creature type choice");
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenTypeWearsOff() {
        addImagecrafter();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addImagecrafter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addImagecrafter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addImagecrafter() {
        Permanent imagecrafter = addCreatureReady(player1, new Imagecrafter());
        harness.forceActivePlayer(player1);
        return imagecrafter;
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}

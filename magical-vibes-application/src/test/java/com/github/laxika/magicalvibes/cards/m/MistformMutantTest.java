package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformMutant.class, GrizzlyBears.class})
class MistformMutantTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen type until end of turn")
    void targetBecomesChosenType() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMutantAndMana();

        activate(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardSubtype.WALL.name());

        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Wall is not a legal creature type choice")
    void wallCannotBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMutantAndMana();

        activate(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, CardSubtype.WALL.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid creature type choice");
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenTypeWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMutantAndMana();

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        target.resetModifiers();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
    }

    private void addMutantAndMana() {
        harness.addToBattlefield(player1, new MistformMutant());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
    }
}

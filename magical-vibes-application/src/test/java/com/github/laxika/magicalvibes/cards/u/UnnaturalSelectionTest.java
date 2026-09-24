package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnaturalSelection.class, AngelfireCrusader.class})
class UnnaturalSelectionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen type until end of turn")
    void targetBecomesChosenType() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelfireCrusader());
        addSelectionAndMana();

        activate(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardSubtype.WALL.name());

        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Wall is not a legal creature type choice")
    void wallCannotBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelfireCrusader());
        addSelectionAndMana();

        activate(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, CardSubtype.WALL.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid creature type choice");
    }

    @Test
    @DisplayName("Only creature types are offered as choices")
    void onlyCreatureTypesAreOffered() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelfireCrusader());
        addSelectionAndMana();

        activate(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);

        assertThat(choice.options()).contains(CardSubtype.ZOMBIE.name(), CardSubtype.FLAGBEARER.name())
                .doesNotContain(CardSubtype.TREASURE.name(), CardSubtype.AURA.name(),
                        CardSubtype.SAGA.name(), CardSubtype.JACE.name(), CardSubtype.LAIR.name(),
                        CardSubtype.ARCANE.name(), CardSubtype.SIEGE.name());
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenTypeWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelfireCrusader());
        var originalSubtypes = gqs.effectiveCreatureSubtypes(gd, target);
        addSelectionAndMana();

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).isEqualTo(originalSubtypes);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        addSelectionAndMana();

        activate(target);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnnaturalSelection());
        addSelectionAndMana();

        assertThatThrownBy(() -> activate(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addSelectionAndMana() {
        harness.addToBattlefield(player1, new UnnaturalSelection());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
    }

    private void activate(Permanent target) {
        Permanent selection = findPermanent(player1, "Unnatural Selection");
        int selectionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(selection);
        harness.activateAbility(player1, selectionIndex, null, target.getId());
        harness.passBothPriorities();
    }
}

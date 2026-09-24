package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrickeryCharm.class, ElvishWarrior.class, Island.class})
class TrickeryCharmTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives target creature flying until end of turn")
    void givesFlyingUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castCharm(player1, 0, creature.getId());

        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying from the first mode wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castCharm(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The second mode changes target creature to the chosen type until end of turn")
    void changesTargetCreatureTypeUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castCharm(player1, 1, creature.getId());
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The creature type from the second mode wears off at end of turn")
    void creatureTypeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castCharm(player1, 1, creature.getId());
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Wall is a legal creature type choice for the second mode")
    void wallIsAValidCreatureTypeChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castCharm(player1, 1, creature.getId());

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains(CardSubtype.WALL.name());

        harness.handleListChoice(player1, CardSubtype.WALL.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("The third mode reorders the top four cards")
    void reordersTopFourCards() {
        List<Card> library = List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(),
                new ElvishWarrior());
        harness.setLibrary(player1, library);

        castCharm(player1, 2, null);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactlyElementsOf(library.subList(0, 4));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                library.get(3), library.get(1), library.get(0), library.get(2), library.get(4));
    }

    @Test
    @DisplayName("The third mode reorders all cards when the library has fewer than four")
    void reordersFewerThanFourCards() {
        List<Card> library = List.of(new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior());
        harness.setLibrary(player1, library);

        castCharm(player1, 2, null);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyElementsOf(library);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                library.get(2), library.get(0), library.get(1));
    }

    @Test
    @DisplayName("Creature modes reject a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> castCharm(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The second mode rejects a noncreature target")
    void secondModeRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> castCharm(player1, 1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCharm(Player caster, int mode, java.util.UUID targetId) {
        harness.setHand(caster, List.of(new TrickeryCharm()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castInstant(caster, 0, mode, targetId);
        harness.passBothPriorities();
    }
}

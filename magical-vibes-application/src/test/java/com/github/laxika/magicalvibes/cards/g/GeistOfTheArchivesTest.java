package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistOfTheArchives.class, GrizzlyBears.class, Forest.class})
class GeistOfTheArchivesTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep triggers scry 1")
    void controllerUpkeepTriggersScry() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's upkeep does not trigger scry")
    void opponentsUpkeepDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void scryCanKeepTopCard() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        GeistOfTheArchives top = new GeistOfTheArchives();
        GeistOfTheArchives next = new GeistOfTheArchives();
        harness.setLibrary(player1, List.of(top, next));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        harness.withAutoStop(TurnStep.UPKEEP, () -> gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of())));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        GeistOfTheArchives top = new GeistOfTheArchives();
        GeistOfTheArchives next = new GeistOfTheArchives();
        harness.setLibrary(player1, List.of(top, next));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryScryCompletesWithoutPrompt() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void defenderCannotAttackEvenWhenReady() {
        var geist = addCreatureReady(player1, new GeistOfTheArchives());

        assertThat(als.canAttack(gd, geist, player1.getId())).isFalse();
    }
}

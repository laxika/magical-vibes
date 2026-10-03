package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContrabandKingpin.class, Ornithopter.class})
class ContrabandKingpinTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering causes you to scry 1")
    void allyArtifactEntryCausesScry() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not cause a scry")
    void opponentArtifactEntryDoesNotCauseScry() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanPutTheTopCardOnTheBottom() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        ContrabandKingpin top = new ContrabandKingpin();
        ContrabandKingpin next = new ContrabandKingpin();
        harness.setLibrary(player1, List.of(top, next));

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryCanKeepTheTopCard() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        ContrabandKingpin top = new ContrabandKingpin();
        ContrabandKingpin next = new ContrabandKingpin();
        harness.setLibrary(player1, List.of(top, next));

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void artifactEntryWithAnEmptyLibraryFinishesWithoutInput() {
        harness.addToBattlefield(player1, new ContrabandKingpin());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ContrabandKingpin.class})
    void nonartifactCreatureEntryDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new ContrabandKingpin());

        harness.castFromHand(player1, new ContrabandKingpin(), "{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Contraband Kingpin")).isEqualTo(2);
    }

    @Test
    @CardUsed({ContrabandKingpin.class})
    void combatDamageGainsLifeForItsController() {
        addCreatureReady(player1, new ContrabandKingpin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}

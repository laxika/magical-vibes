package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigiledSkink.class, GrizzlyBears.class})
class SigiledSkinkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Sigiled Skink triggers scry 1")
    void attackingTriggersScryOne() {
        addCreatureReady(player1, new SigiledSkink());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry can keep the top card without drawing it")
    void keepsTopCard() {
        addCreatureReady(player1, new SigiledSkink());
        SigiledSkink top = new SigiledSkink();
        SigiledSkink next = new SigiledSkink();
        harness.setLibrary(player1, List.of(top, next));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attacking controller can put their top card on the bottom")
    void opponentControllerScriesTheirOwnLibrary() {
        addCreatureReady(player2, new SigiledSkink());
        SigiledSkink top = new SigiledSkink();
        SigiledSkink next = new SigiledSkink();
        SigiledSkink otherLibrary = new SigiledSkink();
        harness.setLibrary(player2, List.of(top, next));
        harness.setLibrary(player1, List.of(otherLibrary));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(top);

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next, top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An attack trigger resolves without a prompt when the library is empty")
    void emptyLibraryDoesNotRequireInput() {
        addCreatureReady(player1, new SigiledSkink());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a nonattacking Sigiled Skink")
    void anotherCreatureAttackingDoesNotTriggerScry() {
        addCreatureReady(player1, new SigiledSkink());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

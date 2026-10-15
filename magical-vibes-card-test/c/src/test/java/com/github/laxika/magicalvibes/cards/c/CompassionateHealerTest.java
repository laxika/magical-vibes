package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompassionateHealer.class, YuyanArchers.class})
class CompassionateHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped gains 1 life and scries 1")
    void becomingTappedGainsLifeAndScries() {
        Permanent healer = addCreatureReady(player1, new CompassionateHealer());
        harness.setLibrary(player1, List.of(new YuyanArchers()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Compassionate Healer")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new CompassionateHealer());
        Permanent otherCreature = addCreatureReady(player1, new YuyanArchers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(1));

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without affecting the opponent")
    void canPutTopCardOnBottom() {
        addCreatureReady(player1, new CompassionateHealer());
        YuyanArchers topCard = new YuyanArchers();
        CompassionateHealer nextCard = new CompassionateHealer();
        YuyanArchers opponentCard = new YuyanArchers();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> gs.handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.ScryOrder(List.of(), List.of(0))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent the life gain")
    void emptyLibraryStillGainsLife() {
        addCreatureReady(player1, new CompassionateHealer());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the Healer that becomes tapped triggers when another Healer stays untapped")
    void onlyTappedHealerTriggers() {
        Permanent attackingHealer = addCreatureReady(player1, new CompassionateHealer());
        Permanent otherHealer = addCreatureReady(player1, new CompassionateHealer());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(attackingHealer.isTapped()).isTrue();
        assertThat(otherHealer.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}

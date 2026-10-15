package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandanTusker.class, GrizzlyBears.class})
class WakandanTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped gains 1 life and scries 1")
    void becomingTappedGainsLifeAndScries() {
        Permanent tusker = addCreatureReady(player1, new WakandanTusker());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(tusker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Wakandan Tusker")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new WakandanTusker());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(1));

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillGainsLife() {
        addCreatureReady(player1, new WakandanTusker());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        addCreatureReady(player1, new WakandanTusker());
        WakandanTusker topCard = new WakandanTusker();
        WakandanTusker nextCard = new WakandanTusker();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void eachAttackingTuskerTriggersOnlyForItself() {
        addCreatureReady(player1, new WakandanTusker());
        addCreatureReady(player1, new WakandanTusker());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
}

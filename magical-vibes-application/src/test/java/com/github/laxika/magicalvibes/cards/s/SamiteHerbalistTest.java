package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YavimayaSojourner;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamiteHerbalist.class, YavimayaSojourner.class})
class SamiteHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped gains 1 life and scries 1")
    void becomingTappedGainsLifeAndScries() {
        Permanent herbalist = addCreatureReady(player1, new SamiteHerbalist());
        harness.setLibrary(player1, List.of(new YavimayaSojourner()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(herbalist.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Samite Herbalist")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new SamiteHerbalist());
        Permanent otherCreature = addCreatureReady(player1, new YavimayaSojourner());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(1));

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent gaining life")
    void emptyLibraryStillGainsLife() {
        addCreatureReady(player1, new SamiteHerbalist());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Herbalist triggers only for itself when both attack")
    void twoHerbalistsEachTriggerOnce() {
        addCreatureReady(player1, new SamiteHerbalist());
        addCreatureReady(player1, new SamiteHerbalist());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability resolves after Samite Herbalist leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent herbalist = addCreatureReady(player1, new SamiteHerbalist());
        SamiteHerbalist topCard = new SamiteHerbalist();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(herbalist);
        gd.playerGraveyards.get(player1.getId()).add(herbalist.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent gains life and may put their top card on the bottom")
    void opponentsHerbalistUsesItsControllersLibraryAndLife() {
        addCreatureReady(player2, new SamiteHerbalist());
        SamiteHerbalist topCard = new SamiteHerbalist();
        SamiteHerbalist secondCard = new SamiteHerbalist();
        harness.setLibrary(player2, List.of(topCard, secondCard));
        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

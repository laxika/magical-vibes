package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WebOfLifeAndDestiny.class, GrizzlyBears.class, Shock.class})
class WebOfLifeAndDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat offers a creature from the top five")
    void offersCreatureFromTopFive() {
        GrizzlyBears creature = new GrizzlyBears();
        setupWebAndLibrary(creature, new Shock(), new Shock(), new Shock(), new Shock());

        advanceToCombat(player1);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield and bottoms the rest")
    void choosingCreaturePutsItOntoBattlefield() {
        GrizzlyBears creature = new GrizzlyBears();
        setupWebAndLibrary(creature, new Shock(), new Shock(), new Shock(), new Shock());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(creature.getId())));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new WebOfLifeAndDestiny());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupWebAndLibrary(Card... topCards) {
        harness.addToBattlefield(player1, new WebOfLifeAndDestiny());
        harness.setLibrary(player1, List.of(topCards));
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

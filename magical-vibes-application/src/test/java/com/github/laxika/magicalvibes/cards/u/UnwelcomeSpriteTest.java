package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnwelcomeSprite.class, GrizzlyBears.class, DarkRitual.class})
class UnwelcomeSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell during an opponent's turn triggers surveil 2")
    void castsSpellDuringOpponentsTurnSurveilsTwo() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, top1);
        gd.playerDecks.get(player1.getId()).add(0, top0);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
    }

    @Test
    @DisplayName("Casting a spell during your own turn does not trigger surveil")
    void castsSpellDuringOwnTurnDoesNotSurveil() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger surveil")
    void opponentsSpellDoesNotSurveil() {
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil can keep both cards in a different order")
    void surveilCanReorderBothCardsOnTop() {
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil can put just one of the two cards into the graveyard")
    void surveilCanSplitCardsBetweenLibraryAndGraveyard() {
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
    }

    @Test
    @DisplayName("Surveil with only one card in the library can put it into the graveyard")
    void surveilWithOneCardInLibrary() {
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }
}

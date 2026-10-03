package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyVisit.class, GrizzlyBears.class, Island.class, BartizanBats.class})
class DeadlyVisitTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and surveils two")
    void destroysTargetCreatureAndSurveilsTwo() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Surveil may keep both cards in reverse order without touching the third")
    void keepsBothCardsInChosenOrder() {
        Permanent bats = harness.addToBattlefieldAndReturn(player1, new BartizanBats());
        Card first = new DeadlyVisit();
        Card second = new BartizanBats();
        Card third = new DeadlyVisit();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bats.getId());

        harness.assertInGraveyard(player1, "Bartizan Bats");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("Surveil may put both cards into the graveyard")
    void putsBothCardsIntoGraveyard() {
        Permanent bats = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        Card first = new BartizanBats();
        Card second = new DeadlyVisit();
        Card third = new BartizanBats();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bats.getId());
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Surveil uses the only card in a one-card library")
    void surveilsOneCardWhenLibraryHasOnlyOne() {
        Permanent bats = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        Card onlyCard = new BartizanBats();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bats.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    @Test
    @DisplayName("An empty library does not prevent destroying the creature")
    void destroysCreatureWithEmptyLibrary() {
        Permanent bats = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bats.getId());

        harness.assertNotOnBattlefield(player2, "Bartizan Bats");
        harness.assertInGraveyard(player2, "Bartizan Bats");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Deadly Visit");
    }

    @Test
    @DisplayName("Does not surveil when its only target has left the battlefield")
    void doesNotSurveilWithIllegalTarget() {
        Permanent bats = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        Card first = new BartizanBats();
        Card second = new DeadlyVisit();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DeadlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, bats.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bats);
        gd.playerGraveyards.get(player2.getId()).add(bats.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        harness.assertInGraveyard(player1, "Deadly Visit");
    }
}

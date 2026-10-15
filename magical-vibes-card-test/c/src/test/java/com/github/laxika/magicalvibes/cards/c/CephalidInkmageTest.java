package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidInkmage.class, GrizzlyBears.class})
class CephalidInkmageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 3")
    void entersWithSurveilThree() {
        Card topCard = new GrizzlyBears();
        Card middleCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, middleCard, bottomCard));
        harness.setHand(player1, List.of(new CephalidInkmage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, middleCard, bottomCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(middleCard, bottomCard);
    }

    @Test
    @DisplayName("Can be blocked below threshold")
    void canBeBlockedBelowThreshold() {
        Permanent inkmage = addReadyInkmage(player1);
        Permanent blocker = addReadyCreature(player2);
        inkmage.setAttacking(true);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be blocked at threshold")
    void cannotBeBlockedAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent inkmage = addReadyInkmage(player1);
        Permanent blocker = addReadyCreature(player2);
        inkmage.setAttacking(true);

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent inkmage = addReadyInkmage(player1);
        Permanent blocker = addReadyCreature(player2);
        inkmage.setAttacking(true);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Surveil may keep and reorder all three cards without disturbing the rest")
    void surveilKeepsAndReordersAllCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.enterBattlefieldAndReturn(player1, new CephalidInkmage());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil with fewer than three cards may put all available cards into the graveyard")
    void surveilShortLibraryAndEnablesThreshold() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent inkmage = harness.enterBattlefieldAndReturn(player1, new CephalidInkmage());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7).contains(first, second);
        inkmage.setSummoningSick(false);
        inkmage.setAttacking(true);
        Permanent blocker = addReadyCreature(player2);
        beginBlockerDeclaration();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Surveil on an empty library finishes without a choice or a loss")
    void surveilEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new CephalidInkmage());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Dropping from seven graveyard cards to six restores blockability")
    void losingThresholdRestoresBlockability() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent inkmage = addReadyInkmage(player1);
        Permanent blocker = addReadyCreature(player2);
        inkmage.setAttacking(true);
        beginBlockerDeclaration();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");

        gd.playerGraveyards.get(player1.getId()).removeFirst();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, inkmage))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyInkmage(Player player) {
        Permanent inkmage = harness.addToBattlefieldAndReturn(player, new CephalidInkmage());
        inkmage.setSummoningSick(false);
        return inkmage;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}

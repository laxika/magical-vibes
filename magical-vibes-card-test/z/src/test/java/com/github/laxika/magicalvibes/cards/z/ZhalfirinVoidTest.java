package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhalfirinVoid.class, Opt.class})
class ZhalfirinVoidTest extends BaseCardTest {

    @BeforeEach
    void setUpLibraries() {
        harness.setLibrary(player1, List.of(new Opt(), new ZhalfirinVoid()));
        harness.setLibrary(player2, List.of(new ZhalfirinVoid(), new Opt()));
    }

    @Test
    @DisplayName("Playing Zhalfirin Void puts ETB trigger on the stack")
    void playingPutsEtbTriggerOnStack() {
        playZhalfirinVoid(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard())
                .isSameAs(findPermanent(player1, "Zhalfirin Void").getCard());
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 1 card")
    void resolvingEtbEntersScryState() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        playZhalfirinVoid(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it to bottom")
    void scryPutOnBottom() {
        playZhalfirinVoid(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Zhalfirin Void enters the battlefield as a permanent")
    void entersBattlefieldAsPermanent() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertOnBattlefield(player1, "Zhalfirin Void");
    }

    @Test
    @DisplayName("Stack is empty after ETB and scry fully resolve")
    void stackEmptyAfterResolution() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana ability resolves immediately while the entry trigger waits on the stack")
    void canTapForColorlessManaBeforeScryResolves() {
        playZhalfirinVoid(player1);
        var trigger = gd.stack.getFirst();
        var permanent = findPermanent(player1, "Zhalfirin Void");
        assertThat(permanent.isTapped()).isFalse();

        harness.tapPermanent(player1, 0);

        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).containsExactly(trigger);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Scry with an empty library finishes without requesting a choice")
    void emptyLibraryScryFinishesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        playZhalfirinVoid(player1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Zhalfirin Void");
    }

    @Test
    @DisplayName("Putting the only library card on the bottom preserves that card")
    void singleCardLibraryCanPutCardOnBottom() {
        Card onlyCard = new Opt();
        harness.setLibrary(player1, List.of(onlyCard));
        playZhalfirinVoid(player1);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land's controller scries their own library, leaving the opponent's untouched")
    void secondPlayerScriesOwnLibrary() {
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<Card> controllerLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        playZhalfirinVoid(player2);
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(interaction.playerId()).isEqualTo(player2.getId());
        assertThat(interaction.cards()).containsExactly(controllerLibrary.getFirst());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(controllerLibrary.get(1), controllerLibrary.getFirst());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    private void playZhalfirinVoid(Player player) {
        harness.setHand(player, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }
}

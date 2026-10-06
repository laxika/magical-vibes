package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigiledStarfish.class})
class SigiledStarfishTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability costs no mana, taps the Starfish and uses the stack")
    void activatingTapsAndUsesStack() {
        Permanent starfish = addCreatureReady(player1, new SigiledStarfish());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(starfish.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability scries exactly one card")
    void resolvingScriesOne() {
        addCreatureReady(player1, new SigiledStarfish());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Keeping the scried card leaves it on top")
    void scryKeepOnTop() {
        addCreatureReady(player1, new SigiledStarfish());
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.getFirst()).isSameAs(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoming the scried card moves it to the bottom of the library")
    void scryBottom() {
        addCreatureReady(player1, new SigiledStarfish());
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("A summoning-sick Starfish cannot activate its tap ability")
    void summoningSickCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new SigiledStarfish()).setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already-tapped Starfish cannot activate its tap ability")
    void tappedCannotActivate() {
        addCreatureReady(player1, new SigiledStarfish()).setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scrying an empty library completes without a choice or a draw")
    void emptyLibraryNeedsNoChoice() {
        addCreatureReady(player1, new SigiledStarfish());
        harness.setLibrary(player1, List.of());
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bottoming the only card leaves it as the only card in the library")
    void bottomOnlyCard() {
        addCreatureReady(player1, new SigiledStarfish());
        Card onlyCard = new SigiledStarfish();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability still scries its controller's library after the source leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent starfish = addCreatureReady(player1, new SigiledStarfish());
        Card top = new SigiledStarfish();
        Card next = new SigiledStarfish();
        Card opponentTop = new SigiledStarfish();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(starfish);
        harness.setGraveyard(player1, List.of(starfish.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

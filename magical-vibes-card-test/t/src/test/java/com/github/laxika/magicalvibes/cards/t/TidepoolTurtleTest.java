package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TidepoolTurtle.class})
class TidepoolTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability pays {2}{U} without tapping Tidepool Turtle")
    void activatingPaysManaWithoutTapping() {
        Permanent turtle = addReadyTurtle();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(turtle.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability starts a one-card scry")
    void resolvingStartsScryOne() {
        addReadyTurtle();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scrying can put the top card on the bottom of the library")
    void scryCanBottomTopCard() {
        addReadyTurtle();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        List<Card> library = gd.playerDecks.get(player1.getId());
        Card top = library.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(library.getFirst()).isNotSameAs(top);
        assertThat(library.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("The ability cannot be activated without {2}{U}")
    void cannotActivateWithoutMana() {
        addReadyTurtle();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyTurtle() {
        return addCreatureReady(player1, new TidepoolTurtle());
    }

    @Test
    @DisplayName("Scrying can leave the top card and the rest of the library unchanged")
    void scryCanKeepTopCard() {
        addReadyTurtle();
        Card top = new TidepoolTurtle();
        Card second = new TidepoolTurtle();
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scrying an empty library resolves without requiring a choice")
    void scryEmptyLibrary() {
        addReadyTurtle();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Turtle can activate its ability repeatedly")
    void tappedSummoningSickTurtleCanActivateRepeatedly() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new TidepoolTurtle());
        turtle.setSummoningSick(true);
        turtle.tap();
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(turtle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activated ability still resolves after Tidepool Turtle leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent turtle = addReadyTurtle();
        Card top = new TidepoolTurtle();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(turtle);
        gd.playerGraveyards.get(player1.getId()).add(turtle.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}

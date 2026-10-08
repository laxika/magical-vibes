package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WitnessOfTomorrows.class})
class WitnessOfTomorrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability pays {3}{U} without tapping Witness of Tomorrows")
    void activatingPaysManaWithoutTapping() {
        Permanent witness = addReadyWitness();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(witness.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability starts a one-card scry")
    void resolvingStartsScryOne() {
        addReadyWitness();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scrying can put the top card on the bottom of the library")
    void scryCanBottomTopCard() {
        Permanent witness = addReadyWitness();
        witness.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

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
    @DisplayName("The ability cannot be activated without {3}{U}")
    void cannotActivateWithoutMana() {
        addReadyWitness();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Scrying can keep the top card without drawing it or changing the opponent's library")
    void scryCanKeepTopCard() {
        addReadyWitness();
        Card top = new WitnessOfTomorrows();
        Card second = new WitnessOfTomorrows();
        Card opposingTop = new WitnessOfTomorrows();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opposingTop));
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
    }

    @Test
    @DisplayName("Scrying an empty library completes without requiring a choice")
    void scryEmptyLibraryCompletes() {
        addReadyWitness();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent witness = addReadyWitness();
        witness.setSummoningSick(true);
        witness.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(witness.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and survives its source leaving the battlefield")
    void repeatedActivationsSurviveSourceLeaving() {
        addReadyWitness();
        Card first = new WitnessOfTomorrows();
        Card second = new WitnessOfTomorrows();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                gd.playerBattlefields.get(player1.getId()).getFirst()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four generic mana cannot pay the required blue mana")
    void cannotActivateWithoutBlueMana() {
        addReadyWitness();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }
    private Permanent addReadyWitness() {
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new WitnessOfTomorrows());
        witness.setSummoningSick(false);
        return witness;
    }
}

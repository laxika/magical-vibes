package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatchfulAutomaton.class, GrizzlyBears.class, Island.class})
class WatchfulAutomatonTest extends BaseCardTest {

    @Test
    void activatedAbilityScryOneAndKeepsTopCard() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activatedAbilityScryOneCanPutTopCardOnBottom() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        automaton.setTapped(true);
        automaton.setSummoningSick(true);
        Card top = new WatchfulAutomaton();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(automaton.isTapped()).isTrue();
    }

    @Test
    void canActivateAgainToScryTheNextCard() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        Card first = new WatchfulAutomaton();
        Card second = new WatchfulAutomaton();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(automaton.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void scryWithEmptyLibraryCompletesWithoutAChoice() {
        harness.addToBattlefield(player1, new WatchfulAutomaton());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlueCostWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new WatchfulAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutTheFullGenericCost() {
        harness.addToBattlefield(player1, new WatchfulAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}

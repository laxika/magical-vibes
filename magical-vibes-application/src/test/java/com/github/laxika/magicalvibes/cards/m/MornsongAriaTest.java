package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MornsongAria.class, AngelOfMercy.class, CounselOfTheSoratami.class,
        GrizzlyBears.class, Plains.class, Swamp.class, PsychogenicProbe.class})
class MornsongAriaTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("The normal draw-step draw is prevented for the active player")
    void normalDrawIsPrevented() {
        harness.addToBattlefield(player1, new MornsongAria());

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Active player loses 3 life and searches their library for a card to hand")
    void activePlayerLosesLifeAndTutors() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().cards()).hasSize(3);

        String chosen = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosen));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trigger acts on the player whose draw step it is")
    void triggersOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains(), new Swamp()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Players can't gain life while Mornsong Aria is on the battlefield")
    void playersCantGainLife() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller cannot draw cards from a spell either")
    void spellDrawsArePrevented() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller cannot gain life either")
    void controllerCannotGainLife() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An empty library still gets shuffled and triggers Psychogenic Probe")
    void emptyLibraryStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player1, new MornsongAria());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

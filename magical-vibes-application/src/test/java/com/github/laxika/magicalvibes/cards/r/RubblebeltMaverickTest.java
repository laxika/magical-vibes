package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubblebeltMaverick.class, Mountain.class})
class RubblebeltMaverickTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 2")
    void entersWithSurveilTwo() {
        Card topCard = new RubblebeltMaverick();
        Card secondCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new RubblebeltMaverick()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("Graveyard ability exiles itself and puts a +1/+1 counter on target creature")
    void graveyardAbilityExilesSelfAndPutsCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RubblebeltMaverick());
        bears.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new RubblebeltMaverick()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Rubblebelt Maverick");
    }

    @Test
    @DisplayName("Graveyard ability requires a creature target")
    void graveyardAbilityRequiresCreatureTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new RubblebeltMaverick()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card first = new RubblebeltMaverick();
        Card second = new Mountain();
        Card third = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new RubblebeltMaverick()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsInGraveyard() {
        Card first = new RubblebeltMaverick();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RubblebeltMaverick()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void surveilWithOnlyOneCardInLibrary() {
        Card onlyCard = new Mountain();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new RubblebeltMaverick()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void exileIsPaidBeforeTheCounterResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        Card source = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(source));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(source);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void graveyardAbilityCannotActivateOutsideMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        Card source = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(source));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    void graveyardAbilityCannotActivateDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        Card source = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(source));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    void graveyardAbilityCannotActivateWithNonemptyStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        Card source = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(new RubblebeltMaverick()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }
}

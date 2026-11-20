package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntrailsFeaster.class, ElvishWarrior.class, Shock.class})
class EntrailsFeasterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card from an opponent's graveyard puts a +1/+1 counter on it")
    void exilesCreatureFromAnyGraveyardAndPutsCounterOnItself() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new ElvishWarrior();
        harness.setGraveyard(player2, List.of(creatureCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creatureCard);
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(feaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to exile a card taps it")
    void tapsWhenItDoesNotExileCreatureCard() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new ElvishWarrior();
        harness.setGraveyard(player2, List.of(creatureCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps it when no graveyard holds a creature card")
    void tapsWhenNoCreatureCardIsInAnyGraveyard() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        harness.setGraveyard(player2, List.of(new Shock()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Offers only creature cards, from its controller's graveyard too")
    void canExileCreatureFromOwnGraveyardWhileIgnoringNoncreatureCards() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new ElvishWarrior();
        Card noncreatureCard = new Shock();
        harness.setGraveyard(player1, List.of(creatureCard));
        harness.setGraveyard(player2, List.of(noncreatureCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creatureCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreatureCard);
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(feaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new ElvishWarrior();
        harness.setGraveyard(player2, List.of(creatureCard));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creatureCard);
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exiling a card does not untap an already tapped Entrails Feaster")
    void successfulExileDoesNotUntapAnAlreadyTappedFeaster() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new ElvishWarrior();
        harness.setGraveyard(player1, List.of(creatureCard));

        advanceToUpkeep(player1);
        feaster.tap();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creatureCard);
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(feaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps it when both graveyards are empty")
    void tapsWhenBothGraveyardsAreEmpty() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feaster.isTapped()).isTrue();
    }
}

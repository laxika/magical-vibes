package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntrailsFeaster.class, GrizzlyBears.class, Shock.class})
class EntrailsFeasterTest extends BaseCardTest {

    @Test
    void exilesCreatureFromAnyGraveyardAndPutsCounterOnItself() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new GrizzlyBears();
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
    void tapsWhenItDoesNotExileCreatureCard() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        Card creatureCard = new GrizzlyBears();
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
    void tapsWhenNoCreatureCardIsInAnyGraveyard() {
        Permanent feaster = addCreatureReady(player1, new EntrailsFeaster());
        harness.setGraveyard(player2, List.of(new Shock()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(feaster.isTapped()).isTrue();
    }
}

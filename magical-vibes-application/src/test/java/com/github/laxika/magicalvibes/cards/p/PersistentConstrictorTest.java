package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersistentConstrictor.class, GrizzlyBears.class, Murder.class})
class PersistentConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent upkeep causes life loss and puts a -1/-1 counter on their creature")
    void opponentUpkeepTriggersBothEffects() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The optional target can be declined while life loss still happens")
    void optionalTargetCanBeDeclined() {
        addCreatureReady(player1, new PersistentConstrictor());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only the active player's creatures are legal targets")
    void onlyActivePlayersCreatureCanBeTargeted() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentBears.getId()));
    }

    @Test
    @DisplayName("Persist returns Persistent Constrictor with a -1/-1 counter")
    void persistReturnsWithCounter() {
        Permanent constrictor = addCreatureReady(player1, new PersistentConstrictor());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, constrictor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Persistent Constrictor");
        assertThat(returned).isNotNull();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.PERSIST)).isTrue();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}

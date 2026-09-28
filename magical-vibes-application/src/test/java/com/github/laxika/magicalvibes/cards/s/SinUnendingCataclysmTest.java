package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinUnendingCataclysm.class, GrizzlyBears.class})
class SinUnendingCataclysmTest extends BaseCardTest {

    @Test
    @DisplayName("Removes all counters and enters with twice the number removed")
    void removesCountersAndEntersWithTwiceTheCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownPermanent.setCounterCount(CounterType.CHARGE, 1);
        opposingPermanent.setCounterCount(CounterType.LOYALTY, 3);

        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        harness.setHand(player1, List.of(sin));
        addSinMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownPermanent.getId(), opposingPermanent.getId()));

        assertThat(ownPermanent.getTotalCounterCount()).isZero();
        assertThat(opposingPermanent.getTotalCounterCount()).isZero();
        assertThat(findPermanent(player1, "Sin, Unending Cataclysm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
    }

    @Test
    @DisplayName("Moves its counters to a creature and then shuffles into its owner's library")
    void movesCountersAndShufflesOnDeath() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        harness.setHand(player1, List.of(sin));
        harness.setLibrary(player1, new ArrayList<>());
        addSinMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent sinPermanent = findPermanent(player1, "Sin, Unending Cataclysm");
        sinPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        sinPermanent.setCounterCount(CounterType.CHARGE, 2);
        sinPermanent.setMarkedDamage(10);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sin);
        assertThat(gd.playerDecks.get(player1.getId())).contains(sin);
    }

    private void addSinMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}

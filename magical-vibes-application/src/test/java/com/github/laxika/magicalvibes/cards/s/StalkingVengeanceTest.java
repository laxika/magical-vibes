package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalkingVengeance.class, DoomBlade.class, GrizzlyBears.class})
class StalkingVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to an ally creature's power when it dies")
    void dealsDamageEqualToDyingCreaturePower() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        destroyWithDoomBlade(bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Uses the dying creature's effective power")
    void usesDyingCreatureEffectivePower() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        destroyWithDoomBlade(bears.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        destroyWithDoomBlade(bears.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void destroyWithDoomBlade(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}

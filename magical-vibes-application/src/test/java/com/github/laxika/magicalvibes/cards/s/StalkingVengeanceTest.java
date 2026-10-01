package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
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

@CardUsed({StalkingVengeance.class, DoomBlade.class, LilianaVess.class})
class StalkingVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to an ally creature's power when it dies")
    void dealsDamageEqualToDyingCreaturePower() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent dyingVengeance = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        harness.setLife(player2, 20);

        destroyWithDoomBlade(dyingVengeance.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Uses the dying creature's effective power")
    void usesDyingCreatureEffectivePower() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent dyingVengeance = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        dyingVengeance.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        destroyWithDoomBlade(dyingVengeance.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not trigger when Stalking Vengeance itself dies")
    void doesNotTriggerForItself() {
        Permanent vengeance = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        harness.setLife(player2, 20);

        destroyWithDoomBlade(vengeance.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new StalkingVengeance());
        Permanent opponentVengeance = harness.addToBattlefieldAndReturn(player2, new StalkingVengeance());
        harness.setLife(player2, 20);

        destroyWithDoomBlade(opponentVengeance.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        Permanent sourceVengeance = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        Permanent dyingVengeance = harness.addToBattlefieldAndReturn(player1, new StalkingVengeance());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        destroyWithDoomBlade(dyingVengeance.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(player1.getId(), player2.getId(), liliana.getId())
                .doesNotContain(sourceVengeance.getId());

        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private void destroyWithDoomBlade(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}

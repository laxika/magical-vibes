package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZukosOffense.class, GrizzlyBears.class, Mountain.class, ChandraHopesBeacon.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class ZukosOffenseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player")
    void dealsDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can deal 2 damage to its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Zuko's Offense");
    }

    @Test
    @DisplayName("Can deal lethal damage to a creature its controller controls")
    void dealsDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker, removing two loyalty counters")
    void dealsDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Chandra, Hope's Beacon");
    }

    @Test
    @DisplayName("Deals 2 damage to a battle, removing two defense counters")
    void dealsDamageToBattle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        target.setCounterCount(CounterType.DEFENSE, 3);
        target.setProtectorPlayerId(player1.getId());
        harness.setHand(player1, List.of(new ZukosOffense()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }
}

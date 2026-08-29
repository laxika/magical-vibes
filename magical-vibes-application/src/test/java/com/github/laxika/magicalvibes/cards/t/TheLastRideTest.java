package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheLastRideTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -X/-X based on its controller's life total")
    void scalesDownWithControllerLifeTotal() {
        harness.setLife(player1, 5);
        Permanent ride = addRideReady(player1);

        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(8);

        harness.setLife(player1, 10);

        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pays life and draws a card")
    void paysLifeToDraw() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addRideReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 animates The Last Ride and taps the crew")
    void crewsWithTwoPower() {
        harness.setLife(player1, 5);
        Permanent ride = addRideReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ride.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ride)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(8);
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addRideReady(Player player) {
        Permanent ride = new Permanent(new TheLastRide());
        ride.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ride);
        return ride;
    }
}

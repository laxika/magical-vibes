package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LotusguardDisciple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLastRide.class, Forest.class, LotusguardDisciple.class})
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
        Permanent crew = addCreatureReady(player1, new LotusguardDisciple());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ride.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ride)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(8);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays life immediately and updates its size before the draw resolves")
    void paysLifeBeforeResolution() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent ride = addRideReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(5);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the draw ability with less than two life")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of());
        addRideReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An uncrewed Vehicle survives at a life total above thirteen")
    void uncrewedVehicleDoesNotDieFromLifeTotal() {
        harness.setLife(player1, 20);
        Permanent ride = addRideReady(player1);

        harness.runStateBasedActions();

        assertThat(gqs.isCreature(gd, ride)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ride);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A crewed Vehicle dies when its controller has thirteen life")
    void crewedVehicleDiesWithZeroToughness() {
        harness.setLife(player1, 13);
        Permanent ride = addRideReady(player1);
        Permanent crew = addCreatureReady(player1, new LotusguardDisciple());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ride);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ride.getCard());
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew and the opponent's life does not affect size")
    void summoningSickCreatureCanCrew() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        Permanent ride = addRideReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new LotusguardDisciple());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ride)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ride)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ride)).isEqualTo(8);
    }

    private Permanent addRideReady(Player player) {
        return addCreatureReady(player, new TheLastRide());
    }
}

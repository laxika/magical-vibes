package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NettleDrone.class, Ornithopter.class, GrizzlyBears.class})
class NettleDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when activated")
    void dealsDamageToEachOpponent() {
        Permanent drone = addReadyDrone();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Untaps when you cast a colorless spell")
    void untapsWhenColorlessSpellIsCast() {
        Permanent drone = addReadyDrone();
        drone.tap();
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap when you cast a colored spell")
    void doesNotUntapWhenColoredSpellIsCast() {
        Permanent drone = addReadyDrone();
        drone.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A spell with devoid untaps the drone before the spell resolves")
    void devoidSpellUntapsBeforeResolving() {
        Permanent drone = addReadyDrone();
        drone.tap();
        harness.setHand(player1, List.of(new NettleDrone()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(drone.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's colorless spell does not untap the drone")
    void opponentColorlessSpellDoesNotUntap() {
        Permanent drone = addReadyDrone();
        drone.tap();
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new NettleDrone()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can deal damage again after its cast trigger untaps it")
    void canActivateAgainAfterUntapping() {
        Permanent drone = addReadyDrone();
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new NettleDrone()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyDrone() {
        return addCreatureReady(player1, new NettleDrone());
    }
}

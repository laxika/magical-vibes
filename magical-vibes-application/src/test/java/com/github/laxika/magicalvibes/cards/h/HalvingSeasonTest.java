package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinGangLeader;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.t.ThrivingRhino;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalvingSeason.class, GoblinGangLeader.class, IchorRats.class, ThrivingRhino.class})
class HalvingSeasonTest extends BaseCardTest {

    @Test
    void halvesTokensCreatedByAnOpponent() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player2, List.of(new GoblinGangLeader()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Goblin")).hasSize(1);
    }

    @Test
    void halvesCountersPutByAnOpponent() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player2, List.of(new IchorRats()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void doesNotHalveCountersPutByItsController() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void doesNotHalveTokensCreatedByItsController() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player1, List.of(new GoblinGangLeader()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    void multipleSeasonsRoundDownAfterEachReplacement() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player2, List.of(new GoblinGangLeader()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    void halvesEnergyCountersReceivedByAnOpponent() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player2, List.of(new ThrivingRhino()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void doesNotHalveEnergyCountersReceivedByItsController() {
        harness.addToBattlefield(player1, new HalvingSeason());
        harness.setHand(player1, List.of(new ThrivingRhino()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    void halvesCountersPlacedOnAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new HalvingSeason());
        Permanent rhino = addCreatureReady(player2, new ThrivingRhino());
        gd.playerEnergyCounters.put(player2.getId(), 2);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}

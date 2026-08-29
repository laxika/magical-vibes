package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UginTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("+2 deals 3 damage to any target")
    void plusTwoDealsDamageToPlayer() {
        Permanent ugin = addReadyUgin(4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-X exiles colored permanents with mana value X or less")
    void minusXExilesMatchingPermanents() {
        Permanent ugin = addReadyUgin(5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new AirElemental());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-10 gains life, draws seven, and puts at most seven permanents from hand onto the battlefield")
    void minusTenPutsAtMostSevenPermanentsFromHand() {
        Permanent ugin = addReadyUgin(10);
        List<Card> hand = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(27);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(7);
        harness.assertNotOnBattlefield(player1, "Ugin, the Spirit Dragon");
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyUgin(int loyalty) {
        Permanent ugin = harness.addToBattlefieldAndReturn(player1, new UginTheSpiritDragon());
        ugin.setCounterCount(CounterType.LOYALTY, loyalty);
        ugin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ugin;
    }
}

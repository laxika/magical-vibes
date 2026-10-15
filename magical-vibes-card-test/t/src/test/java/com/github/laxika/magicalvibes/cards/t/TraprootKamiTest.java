package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShimmeringGlasskite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraprootKami.class, Forest.class, Plains.class, ShimmeringGlasskite.class})
class TraprootKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals the number of Forests on the battlefield")
    void toughnessEqualsForestsOnBattlefield() {
        Permanent kami = addCreatureReady(player1, new TraprootKami());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, kami)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Toughness updates as Forests enter and leave the battlefield")
    void toughnessUpdatesWhenForestsChange() {
        Permanent kami = addCreatureReady(player1, new TraprootKami());

        assertThat(gqs.getEffectiveToughness(gd, kami)).isZero();

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectiveToughness(gd, kami)).isZero();
    }

    @Test
    void diesAfterResolvingWithNoForests() {
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new TraprootKami(), "{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Traproot Kami");
        harness.assertInGraveyard(player1, "Traproot Kami");
    }

    @Test
    void survivesWithOnlyOpponentsForests() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new TraprootKami(), "{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Traproot Kami");
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Traproot Kami"))).isEqualTo(1);
    }

    @Test
    void characteristicToughnessWorksInHandAndGraveyard() {
        TraprootKami inHand = new TraprootKami();
        TraprootKami inGraveyard = new TraprootKami();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
    }

    @Test
    void countersModifyCharacteristicPowerAndToughness() {
        harness.addToBattlefield(player1, new Forest());
        Permanent kami = addCreatureReady(player1, new TraprootKami());
        kami.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(3);
    }

    @Test
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new TraprootKami());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new ShimmeringGlasskite());
        Permanent kami = addCreatureReady(player2, new TraprootKami());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(kami.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Traproot Kami");
    }
}

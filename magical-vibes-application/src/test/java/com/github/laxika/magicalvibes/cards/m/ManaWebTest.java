package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GemstoneMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaWeb.class, AdarkarWastes.class, Forest.class, GemstoneMine.class, Island.class, Mountain.class})
class ManaWebTest extends BaseCardTest {

    @Test
    @DisplayName("A land tap queues Mana Web and resolution taps matching lands only")
    void tapsMatchingOpponentLandsOnResolution() {
        harness.addToBattlefield(player1, new ManaWeb());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(firstForest.isTapped()).isTrue();
        assertThat(secondForest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        resolveAllTriggers();

        assertThat(secondForest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent can tap matching lands in response to Mana Web's trigger")
    void opponentCanTapMatchingLandInResponse() {
        harness.addToBattlefield(player1, new ManaWeb());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);

        assertThat(firstForest.isTapped()).isTrue();
        assertThat(secondForest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(2);

        resolveAllTriggers();

        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana Web does not trigger when its controller taps a land")
    void doesNotTriggerForOwnLand() {
        harness.addToBattlefield(player1, new ManaWeb());
        harness.addToBattlefield(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(secondForest.isTapped()).isFalse();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("Mana Web checks the triggering land's available types, not only the mana chosen")
    void checksAllTypesTheTriggeringLandCouldProduce() {
        harness.addToBattlefield(player1, new ManaWeb());
        Permanent wastes = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(wastes.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        assertThat(island.isTapped()).isFalse();

        resolveAllTriggers();

        assertThat(forest.isTapped()).isFalse();
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forced taps do not produce mana or trigger Mana Web again")
    void forcedTapsDoNotProduceManaOrRetrigger() {
        harness.addToBattlefield(player1, new ManaWeb());
        harness.addToBattlefield(player2, new AdarkarWastes());
        Permanent otherWastes = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(otherWastes.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(ownIsland.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana Web uses last known mana types when the triggering land leaves")
    void usesLastKnownManaTypesAfterTriggeringLandLeaves() {
        harness.addToBattlefield(player1, new ManaWeb());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.tapPermanent(player2, 0);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, firstForest);
        resolveAllTriggers();

        assertThat(secondForest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gemstone Mine's final use still causes Mana Web to tap every colored land")
    void usesLastKnownManaTypesWhenMineSacrificesItself() {
        harness.addToBattlefield(player1, new ManaWeb());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new GemstoneMine());
        mine.setCounterCount(CounterType.MINING, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");
        harness.assertNotOnBattlefield(player2, "Gemstone Mine");
        resolveAllTriggers();

        assertThat(forest.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }
}

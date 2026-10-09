package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalDiscovery.class, Forest.class})
class CoastalDiscoveryTest extends BaseCardTest {

    @Test
    void drawsTwoCards() {
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Coastal Discovery");
    }

    @Test
    void alternateCastDrawsTwoCardsAndAwakensTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void normalCastDoesNotAwakenALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void doesNotDrawWhenAwakenTargetLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Coastal Discovery");
    }

    @Test
    void doesNotDrawWhenAwakenTargetChangesController() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        harness.assertInGraveyard(player1, "Coastal Discovery");
    }

    @Test
    void cannotAwakenOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenPersistsAfterTurnEnds() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void awakeningAnAlreadyAwakenedLandAddsCountersAndDoesNotUntapIt() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.setHand(player1, List.of(new CoastalDiscovery(), new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
        assertThat(land.isTapped()).isTrue();
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanRestorer.class, Forest.class, GrizzlyBears.class})
class KrosanRestorerTest extends BaseCardTest {

    @Test
    @DisplayName("Basic ability untaps target land")
    void untapsTargetLand() {
        Permanent restorer = addCreatureReady(player1, new KrosanRestorer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(restorer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Threshold ability untaps up to three target lands")
    void thresholdUntapsThreeTargetLands() {
        addCreatureReady(player1, new KrosanRestorer());
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player2, new Forest()));
        lands.forEach(Permanent::tap);
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                lands.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(lands).allMatch(land -> !land.isTapped());
    }

    @Test
    @DisplayName("Threshold ability accepts fewer than three targets")
    void thresholdAcceptsFewerTargets() {
        addCreatureReady(player1, new KrosanRestorer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Threshold ability requires seven cards in the graveyard")
    void thresholdRequiresSevenGraveyardCards() {
        addCreatureReady(player1, new KrosanRestorer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addCreatureReady(player1, new KrosanRestorer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

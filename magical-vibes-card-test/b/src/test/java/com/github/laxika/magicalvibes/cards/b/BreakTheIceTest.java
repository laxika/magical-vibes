package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakTheIce.class, Forest.class, SnowCoveredForest.class, Wastes.class})
class BreakTheIceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a land that could produce colorless mana")
    void destroysLandThatCouldProduceColorlessMana() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player2, new Wastes());
        castBreakTheIce(wastes.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wastes);
    }

    @Test
    @DisplayName("Destroys a snow land even when it produces only colored mana")
    void destroysSnowLand() {
        Permanent snowForest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        castBreakTheIce(snowForest.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(snowForest);
    }

    @Test
    @DisplayName("Rejects a nonsnow land that cannot produce colorless mana")
    void rejectsOrdinaryColoredLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow land or a land that could produce colorless mana");
    }

    @Test
    @DisplayName("Overload destroys every qualifying land and no other permanents")
    void overloadDestroysEveryQualifyingLand() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player2, new Wastes());
        Permanent snowForest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wastes, snowForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("A tapped colorless-producing land remains a legal target")
    void destroysTappedColorlessProducingLand() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player2, new Wastes());
        wastes.tap();

        castBreakTheIce(wastes.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wastes);
        harness.assertInGraveyard(player2, "Wastes");
    }

    @Test
    @DisplayName("Normal casting destroys only the chosen land, including your own land")
    void destroysOnlyChosenLandYouControl() {
        Permanent ownWastes = harness.addToBattlefieldAndReturn(player1, new Wastes());
        Permanent otherWastes = harness.addToBattlefieldAndReturn(player2, new Wastes());
        Permanent snowForest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());

        castBreakTheIce(ownWastes.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownWastes);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherWastes, snowForest);
        harness.assertInGraveyard(player1, "Wastes");
    }

    @Test
    @DisplayName("Overload also destroys your qualifying lands")
    void overloadDestroysQualifyingLandsOfBothPlayers() {
        Permanent ownWastes = harness.addToBattlefieldAndReturn(player1, new Wastes());
        Permanent ownSnowForest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        Permanent otherWastes = harness.addToBattlefieldAndReturn(player2, new Wastes());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownWastes, ownSnowForest);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(otherWastes);
        harness.assertInGraveyard(player1, "Wastes");
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Wastes");
    }

    @Test
    @DisplayName("Overload can be cast and resolve without any qualifying land")
    void overloadResolvesWithoutQualifyingLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Break the Ice");
        assertThat(gd.stack).isEmpty();
    }

    private void castBreakTheIce(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}

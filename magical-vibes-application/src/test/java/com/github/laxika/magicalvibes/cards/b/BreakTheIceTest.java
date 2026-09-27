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

    private void castBreakTheIce(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BreakTheIce()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}

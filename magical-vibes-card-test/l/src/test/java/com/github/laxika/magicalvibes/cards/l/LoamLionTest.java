package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoamLion.class, Forest.class})
class LoamLionTest extends BaseCardTest {

    @Test
    @DisplayName("Has base 1/1 without a Forest")
    void noBoostWithoutForest() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+2 while its controller controls a Forest")
    void boostWithForest() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Forest does not grant the boost")
    void noBoostFromOpponentForest() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when its controller no longer controls a Forest")
    void losesBoostWhenForestLeaves() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(forest);

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Forests grant only one boost, which remains while one Forest is controlled")
    void multipleForestsDoNotStackBoost() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(forest);

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Forests in hand and graveyard do not grant the boost")
    void forestsOutsideBattlefieldDoNotGrantBoost() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
    }
}

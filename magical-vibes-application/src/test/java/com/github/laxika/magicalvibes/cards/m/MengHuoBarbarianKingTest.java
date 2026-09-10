package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MengHuoBarbarianKing.class, ForestBear.class, WeiInfantry.class})
class MengHuoBarbarianKingTest extends BaseCardTest {

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOtherGreenCreatures() {
        Permanent forestBear = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());

        assertThat(gqs.getEffectivePower(gd, forestBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forestBear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Meng Huo does not buff itself")
    void doesNotBuffItself() {
        Permanent mengHuo = harness.addToBattlefieldAndReturn(player1, new MengHuoBarbarianKing());

        assertThat(gqs.getEffectivePower(gd, mengHuo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mengHuo)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Meng Huos buff each other but not themselves")
    void twoMengHuosBuffEachOtherButNotThemselves() {
        Permanent firstMengHuo = harness.addToBattlefieldAndReturn(player1, new MengHuoBarbarianKing());
        Permanent secondMengHuo = harness.addToBattlefieldAndReturn(player1, new MengHuoBarbarianKing());

        assertThat(gqs.getEffectivePower(gd, firstMengHuo)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstMengHuo)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondMengHuo)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondMengHuo)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff non-green creatures")
    void doesNotBuffNonGreenCreatures() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        Permanent weiInfantry = harness.addToBattlefieldAndReturn(player1, new WeiInfantry());

        assertThat(gqs.getEffectivePower(gd, weiInfantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, weiInfantry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not buff opponent's green creatures")
    void doesNotBuffOpponentGreenCreatures() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        Permanent opponentForestBear = harness.addToBattlefieldAndReturn(player2, new ForestBear());

        assertThat(gqs.getEffectivePower(gd, opponentForestBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentForestBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Meng Huo leaves the battlefield")
    void bonusRemovedWhenMengHuoLeaves() {
        Permanent mengHuo = harness.addToBattlefieldAndReturn(player1, new MengHuoBarbarianKing());
        Permanent forestBear = harness.addToBattlefieldAndReturn(player1, new ForestBear());

        assertThat(gqs.getEffectivePower(gd, forestBear)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mengHuo));

        assertThat(gqs.getEffectivePower(gd, forestBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forestBear)).isEqualTo(2);
    }
}

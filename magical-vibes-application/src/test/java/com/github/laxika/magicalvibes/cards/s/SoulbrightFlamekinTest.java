package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulbrightFlamekinTest extends BaseCardTest {

    @Test
    @DisplayName("Each activation grants trample to the target; no mana burst before the third")
    void grantsTrampleNoManaBeforeThird() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        activateTargeting(bears);
        activateTargeting(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Third resolution adds eight red mana")
    void thirdResolutionAddsEightRed() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 6);

        activateTargeting(bears);
        activateTargeting(bears);
        activateTargeting(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(8);
    }

    @Test
    @DisplayName("Targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent forest = addCreatureReady(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateTargeting(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}

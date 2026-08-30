package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MobRuleTest extends BaseCardTest {

    @Test
    @DisplayName("The high-power mode steals, untaps, and gives haste to creatures with power 4 or greater")
    void highPowerMode() {
        Permanent large = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent small = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        large.tap();
        small.tap();

        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(large.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(small.getId())
                .doesNotContain(large.getId());
        assertThat(large.isTapped()).isFalse();
        assertThat(large.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(small.isTapped()).isTrue();
        assertThat(small.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The low-power mode includes power 3 and excludes power 4")
    void lowPowerMode() {
        Permanent small = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boundary = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent large = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        small.tap();
        boundary.tap();
        large.tap();

        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(small.getId(), boundary.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(large.getId());
        assertThat(small.isTapped()).isFalse();
        assertThat(boundary.isTapped()).isFalse();
        assertThat(large.isTapped()).isTrue();
        assertThat(small.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(boundary.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(large.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Temporary control and haste expire at cleanup")
    void effectsExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new MobRule()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InnerFlameIgniterTest extends BaseCardTest {

    @Test
    @DisplayName("Each activation gives +1/+0 to creatures you control; no first strike before the third")
    void pumpsWithoutFirstStrikeBeforeThird() {
        addIgniter(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 6);

        activateAndResolve();
        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Third resolution grants first strike to creatures you control (plus the +1/+0)")
    void thirdResolutionGrantsFirstStrike() {
        addIgniter(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 9);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void opponentCreaturesUnaffected() {
        addIgniter(player1);
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 9);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        assertThat(enemyBears.getPowerModifier()).isEqualTo(0);
        assertThat(enemyBears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addIgniter(Player player) {
        Permanent perm = new Permanent(new InnerFlameIgniter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

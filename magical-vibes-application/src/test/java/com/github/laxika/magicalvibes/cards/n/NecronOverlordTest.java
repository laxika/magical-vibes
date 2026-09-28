package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecronOverlord.class, Ornithopter.class})
class NecronOverlordTest extends BaseCardTest {

    @Test
    void relentlessMarchMakesOpponentLoseXLifeAndTapsArtifacts() {
        Permanent overlord = addReady(new NecronOverlord());
        Permanent artifact1 = addReady(new Ornithopter());
        Permanent artifact2 = addReady(new Ornithopter());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(overlord), 2,
                player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(overlord.isTapped()).isTrue();
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
    }

    @Test
    void relentlessMarchCannotTargetItsController() {
        Permanent overlord = addReady(new NecronOverlord());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(overlord), 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(overlord.isTapped()).isFalse();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}

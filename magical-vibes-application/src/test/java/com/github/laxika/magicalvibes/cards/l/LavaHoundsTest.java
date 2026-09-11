package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LavaHounds.class)
class LavaHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to you")
    void etbDeals4DamageToController() {
        harness.castFromHand(player1, new LavaHounds(), "{2}{R}{R}");
        harness.setLife(player1, 20);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lava Hounds");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Haste allows attacking the turn it enters")
    void hasteAllowsAttackingImmediately() {
        harness.castFromHand(player1, new LavaHounds(), "{2}{R}{R}");
        resolveAllTriggers();

        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isAttackedThisTurn()).isTrue();
    }
}

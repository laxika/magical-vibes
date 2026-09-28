package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gobland.class, GrizzlyBears.class})
class GoblandTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gobland produces one red mana")
    void tappingProducesRedMana() {
        Permanent gobland = addCreatureReady(player1, new Gobland());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gobland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Gobland cannot tap for mana")
    void summoningSickCannotTap() {
        harness.addToBattlefield(player1, new Gobland());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Gobland cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent gobland = addCreatureReady(player2, new Gobland());

        assertThat(bls.canBlockAttacker(gd, gobland, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}

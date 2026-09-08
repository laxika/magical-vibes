package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LlanowarDead.class)
class LlanowarDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Llanowar Dead produces one black mana")
    void tappingProducesBlackMana() {
        Permanent dead = addLlanowarDead(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(dead.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Llanowar Dead cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent dead = addLlanowarDead(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(dead.isTapped()).isFalse();
    }

    private Permanent addLlanowarDead(boolean summoningSick) {
        Permanent dead = new Permanent(new LlanowarDead());
        dead.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player1.getId()).add(dead);
        return dead;
    }
}

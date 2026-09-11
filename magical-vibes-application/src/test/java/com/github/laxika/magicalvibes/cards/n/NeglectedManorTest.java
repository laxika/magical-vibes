package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeglectedManor.class})
class NeglectedManorTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playManor();

        assertThat(manor().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playManor();

        assertThat(manor().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playManor();

        assertThat(manor().isTapped()).isTrue();
    }

    @Test
    void tappingProducesWhiteMana() {
        Permanent manor = addReadyManor();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(manor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent manor = addReadyManor();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(manor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playManor() {
        harness.setHand(player1, List.of(new NeglectedManor()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyManor() {
        Permanent manor = new Permanent(new NeglectedManor());
        manor.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(manor);
        return manor;
    }

    private Permanent manor() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

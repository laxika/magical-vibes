package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StrangledCemetery.class)
class StrangledCemeteryTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playCemetery();

        assertThat(cemetery().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playCemetery();

        assertThat(cemetery().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playCemetery();

        assertThat(cemetery().isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent cemetery = addReadyCemetery();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cemetery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void tappingProducesGreenMana() {
        Permanent cemetery = addReadyCemetery();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(cemetery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void playCemetery() {
        harness.setHand(player1, List.of(new StrangledCemetery()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCemetery() {
        Permanent cemetery = new Permanent(new StrangledCemetery());
        cemetery.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cemetery);
        return cemetery;
    }

    private Permanent cemetery() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

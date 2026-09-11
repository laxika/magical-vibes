package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazortrapGorge.class})
class RazortrapGorgeTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playGorge();

        assertThat(gorge().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playGorge();

        assertThat(gorge().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playGorge();

        assertThat(gorge().isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent gorge = addReadyGorge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gorge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void tappingProducesRedMana() {
        Permanent gorge = addReadyGorge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gorge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void playGorge() {
        harness.setHand(player1, List.of(new RazortrapGorge()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyGorge() {
        Permanent gorge = new Permanent(new RazortrapGorge());
        gorge.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gorge);
        return gorge;
    }

    private Permanent gorge() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

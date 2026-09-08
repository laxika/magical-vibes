package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaucousCarnival.class})
class RaucousCarnivalTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playCarnival();

        assertThat(carnival().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playCarnival();

        assertThat(carnival().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playCarnival();

        assertThat(carnival().isTapped()).isTrue();
    }

    @Test
    void tappingProducesRedMana() {
        Permanent carnival = addReadyCarnival();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(carnival.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappingProducesWhiteMana() {
        Permanent carnival = addReadyCarnival();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(carnival.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playCarnival() {
        harness.setHand(player1, List.of(new RaucousCarnival()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCarnival() {
        Permanent carnival = new Permanent(new RaucousCarnival());
        carnival.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(carnival);
        return carnival;
    }

    private Permanent carnival() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

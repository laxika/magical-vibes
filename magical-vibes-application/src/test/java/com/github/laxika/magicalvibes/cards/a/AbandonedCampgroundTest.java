package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbandonedCampground.class})
class AbandonedCampgroundTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playCampground();

        assertThat(campground().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playCampground();

        assertThat(campground().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playCampground();

        assertThat(campground().isTapped()).isTrue();
    }

    @Test
    void tappingProducesWhiteMana() {
        Permanent campground = addReadyCampground();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(campground.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent campground = addReadyCampground();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(campground.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playCampground() {
        harness.setHand(player1, List.of(new AbandonedCampground()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCampground() {
        Permanent campground = new Permanent(new AbandonedCampground());
        campground.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campground);
        return campground;
    }

    private Permanent campground() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

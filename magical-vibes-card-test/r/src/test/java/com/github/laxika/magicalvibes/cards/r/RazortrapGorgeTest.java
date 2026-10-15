package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void entersUntappedWhenControllerHasLessThan13Life() {
        harness.setLife(player1, 1);
        playGorge();

        assertThat(gorge().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHasLessThan13Life() {
        harness.setLife(player2, 1);
        playGorge();

        assertThat(gorge().isTapped()).isFalse();
    }

    @Test
    void newlyPlayedUntappedGorgeCanProduceManaImmediately() {
        harness.setLife(player1, 13);
        playGorge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gorge().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gorgeEnteringTappedCannotProduceMana() {
        playGorge();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gorge().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void producingBlackManaPreventsProducingRedManaWithoutUntapping() {
        addReadyGorge();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void playGorge() {
        harness.setHand(player1, List.of(new RazortrapGorge()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyGorge() {
        return addCreatureReady(player1, new RazortrapGorge());
    }

    private Permanent gorge() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

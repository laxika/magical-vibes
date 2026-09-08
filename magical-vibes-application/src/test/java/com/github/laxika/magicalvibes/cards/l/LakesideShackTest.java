package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LakesideShack.class})
class LakesideShackTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playShack();

        assertThat(shack().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playShack();

        assertThat(shack().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playShack();

        assertThat(shack().isTapped()).isTrue();
    }

    @Test
    void tappingProducesGreenMana() {
        Permanent shack = addReadyShack();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shack.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent shack = addReadyShack();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(shack.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playShack() {
        harness.setHand(player1, List.of(new LakesideShack()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyShack() {
        Permanent shack = new Permanent(new LakesideShack());
        shack.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shack);
        return shack;
    }

    private Permanent shack() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}

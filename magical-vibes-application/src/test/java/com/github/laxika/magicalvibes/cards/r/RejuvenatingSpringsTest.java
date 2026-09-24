package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RejuvenatingSprings.class)
class RejuvenatingSpringsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than two opponents")
    void entersTappedWithFewerThanTwoOpponents() {
        playLand();

        assertThat(springs().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with two or more opponents")
    void entersUntappedWithTwoOrMoreOpponents() {
        gd.orderedPlayerIds.add(UUID.randomUUID());

        playLand();

        assertThat(springs().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        Permanent springs = addReadySprings(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(springs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping produces blue mana")
    void tappingProducesBlueMana() {
        Permanent springs = addReadySprings(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(springs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playLand() {
        harness.setHand(player1, List.of(new RejuvenatingSprings()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadySprings(Player player) {
        Permanent springs = new Permanent(new RejuvenatingSprings());
        springs.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(springs);
        return springs;
    }

    private Permanent springs() {
        return findPermanent(player1, "Rejuvenating Springs");
    }
}

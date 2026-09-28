package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed(UndergrowthStadium.class)
class UndergrowthStadiumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than two opponents")
    void entersTappedWithFewerThanTwoOpponents() {
        playLand();

        assertThat(stadium().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with two or more opponents")
    void entersUntappedWithTwoOrMoreOpponents() {
        gd.orderedPlayerIds.add(UUID.randomUUID());

        playLand();

        assertThat(stadium().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces black mana")
    void tappingProducesBlackMana() {
        Permanent stadium = addReadyStadium(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stadium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        Permanent stadium = addReadyStadium(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(stadium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void playLand() {
        harness.setHand(player1, List.of(new UndergrowthStadium()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyStadium(Player player) {
        Permanent stadium = new Permanent(new UndergrowthStadium());
        stadium.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(stadium);
        return stadium;
    }

    private Permanent stadium() {
        return findPermanent(player1, "Undergrowth Stadium");
    }
}

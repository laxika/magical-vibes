package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new UndergrowthStadium());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stadium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new UndergrowthStadium());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(stadium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped with one opponent")
    void entersTappedWithoutBeingPlayed() {
        Permanent stadium = harness.enterBattlefieldAndReturn(player1, new UndergrowthStadium());

        assertThat(stadium.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played enters untapped with two opponents")
    void entersUntappedWithoutBeingPlayedWithTwoOpponents() {
        gd.orderedPlayerIds.add(UUID.randomUUID());

        Permanent stadium = harness.enterBattlefieldAndReturn(player1, new UndergrowthStadium());

        assertThat(stadium.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped stadium cannot produce either color of mana")
    void tappedStadiumCannotProduceMana() {
        playLand();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(stadium().isTapped()).isTrue();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new UndergrowthStadium()));
        harness.playLand(player1, 0);
    }

    private Permanent stadium() {
        return findPermanent(player1, "Undergrowth Stadium");
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EtchedCornfield.class)
class EtchedCornfieldTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when every player has more than 13 life")
    void entersTappedWhenEveryPlayerHasMoreThanThirteenLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);

        playCornfield(player1);

        assertThat(findCornfield(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has 13 life")
    void entersUntappedWhenControllerHasThirteenLife() {
        harness.setLife(player1, 13);
        harness.setLife(player2, 14);

        playCornfield(player1);

        assertThat(findCornfield(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when an opponent has 13 life")
    void entersUntappedWhenOpponentHasThirteenLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 13);

        playCornfield(player1);

        assertThat(findCornfield(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addReadyCornfield(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addReadyCornfield(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playCornfield(Player player) {
        harness.setHand(player, List.of(new EtchedCornfield()));
        harness.playLand(player, 0);
    }

    private Permanent addReadyCornfield(Player player) {
        Permanent permanent = new Permanent(new EtchedCornfield());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findCornfield(Player player) {
        return findPermanent(player, "Etched Cornfield");
    }
}

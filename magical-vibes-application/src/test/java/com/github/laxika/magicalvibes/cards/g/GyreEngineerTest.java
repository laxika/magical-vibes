package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GyreEngineer.class})
class GyreEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gyre Engineer produces one green and one blue mana")
    void tappingProducesGreenAndBlueMana() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GyreEngineer());
        perm.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick Gyre Engineer cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GyreEngineer());
        perm.setSummoningSick(true);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already-tapped Gyre Engineer cannot produce mana again")
    void cannotActivateAgainWhileTapped() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GyreEngineer());
        perm.setSummoningSick(false);
        gs.tapPermanent(gd, player1, 0);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gyre Engineer adds both colors to its controller's pool")
    void secondPlayersEngineerProducesManaForSecondPlayer() {
        Permanent perm = harness.addToBattlefieldAndReturn(player2, new GyreEngineer());
        perm.setSummoningSick(false);

        gs.tapPermanent(gd, player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Plateau.class)
class PlateauTest extends BaseCardTest {

    @Test
    @DisplayName("Plateau produces red mana")
    void producesRedMana() {
        Permanent plateau = addCreatureReady(player1, new Plateau());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(plateau.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Plateau produces white mana")
    void producesWhiteMana() {
        Permanent plateau = addCreatureReady(player1, new Plateau());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(plateau.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Plateau can produce mana immediately after being played without using the stack")
    void producesManaImmediatelyAfterBeingPlayed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Plateau()));

        harness.playLand(player1, 0);
        Permanent plateau = findPermanent(player1, "Plateau");
        assertThat(plateau.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(plateau.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Plateau for one color prevents producing the other color until it untaps")
    void cannotProduceBothColorsFromOneTap() {
        harness.addToBattlefield(player1, new Plateau());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}

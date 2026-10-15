package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TimberGorge.class})
class TimberGorgeTest extends BaseCardTest {

    @Test
    @DisplayName("Timber Gorge enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TimberGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findPermanent(player1, "Timber Gorge");
        assertThat(gorge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        Permanent gorge = harness.addToBattlefieldAndReturn(player1, new TimberGorge());
        gorge.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent gorge = harness.addToBattlefieldAndReturn(player1, new TimberGorge());
        gorge.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Timber Gorge also enters tapped when put onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new TimberGorge());

        assertThat(gorge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither mana ability can be activated while Timber Gorge is tapped")
    void cannotProduceManaWhileTapped() {
        harness.enterBattlefieldAndReturn(player1, new TimberGorge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Timber Gorge can produce mana after untapping")
    void producesManaAfterUntapping() {
        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new TimberGorge());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gorge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}

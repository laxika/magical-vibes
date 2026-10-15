package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Annex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggHollows.class, Annex.class})
class MoggHollowsTest extends BaseCardTest {

    @Test
    @DisplayName("Changing control does not prevent untapping during the new controller's untap step")
    void newControllerCanUntapLandBeforeActivatorsNextTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoggHollows());
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Annex()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player2, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Hollows");
        harness.performUntapStep(player2);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First ability adds colorless without locking the untap step")
    void colorlessAbilityDoesNotSkipUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoggHollows());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isEqualTo(0);

        harness.performUntapStep(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Red ability adds {R} and locks the next untap step")
    void redAbilityAddsRedAndSkipsUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoggHollows());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Green ability adds {G} and locks the next untap step")
    void greenAbilityAddsGreenAndSkipsUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoggHollows());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Colored abilities skip exactly one untap step")
    void coloredAbilitiesSkipExactlyOneUntapStep() {
        Permanent redLand = harness.addToBattlefieldAndReturn(player1, new MoggHollows());
        Permanent greenLand = harness.addToBattlefieldAndReturn(player1, new MoggHollows());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 2, null, null);

        harness.performUntapStep(player1);

        assertThat(redLand.isTapped()).isTrue();
        assertThat(greenLand.isTapped()).isTrue();
        assertThat(redLand.getSkipUntapCount()).isZero();
        assertThat(greenLand.getSkipUntapCount()).isZero();

        harness.performUntapStep(player1);

        assertThat(redLand.isTapped()).isFalse();
        assertThat(greenLand.isTapped()).isFalse();
    }
}

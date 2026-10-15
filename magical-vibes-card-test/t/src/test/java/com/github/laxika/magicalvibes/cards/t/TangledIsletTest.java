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

@CardUsed(TangledIslet.class)
class TangledIsletTest extends BaseCardTest {

    @Test
    @DisplayName("Tangled Islet enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TangledIslet()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tangled Islet").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangled Islet taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    @Test
    @DisplayName("Tangled Islet taps for blue mana")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Tangled Islet enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent islet = harness.enterBattlefieldAndReturn(player1, new TangledIslet());

        assertThat(islet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Tangled Islet cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        Permanent islet = harness.enterBattlefieldAndReturn(player1, new TangledIslet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(islet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Tangled Islet can produce mana the turn it enters if untapped")
    void canActivateTheTurnItEntersWhenUntapped() {
        Permanent islet = harness.enterBattlefieldAndReturn(player1, new TangledIslet());
        islet.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(islet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor color) {
        Permanent islet = addCreatureReady(player1, new TangledIslet());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(islet.isTapped()).isTrue();
    }
}

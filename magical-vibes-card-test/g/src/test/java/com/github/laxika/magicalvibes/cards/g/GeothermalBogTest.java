package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeothermalBog.class})
class GeothermalBogTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GeothermalBog()));

        harness.playLand(player1, 0);

        Permanent bog = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or red mana")
    void manaAbilityAddsBlackOrRedMana() {
        Permanent bog = addReadyBog();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bog.isTapped()).isTrue();

        bog.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyBog() {
        return addCreatureReady(player1, new GeothermalBog());
    }

    @Test
    @DisplayName("Enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent bog = harness.enterBattlefieldAndReturn(player1, new GeothermalBog());

        assertThat(bog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate its mana ability while tapped")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new GeothermalBog()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A newly entered land can produce mana once untapped, without using the stack")
    void newlyEnteredLandCanProduceManaOnceUntapped() {
        harness.setHand(player1, List.of(new GeothermalBog()));
        harness.playLand(player1, 0);
        Permanent bog = gd.playerBattlefields.get(player1.getId()).getFirst();
        bog.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(bog.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

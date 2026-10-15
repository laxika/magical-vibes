package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VectorImperialCapital.class)
class VectorImperialCapitalTest extends BaseCardTest {

    @Test
    @DisplayName("Land enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new VectorImperialCapital()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tappingProducesBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VectorImperialCapital());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tappingProducesRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VectorImperialCapital());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Land also enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new VectorImperialCapital());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves immediately and produces only the chosen color")
    void manaAbilityDoesNotUseStack() {
        harness.addToBattlefield(player1, new VectorImperialCapital());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == ManaColor.BLACK ? 1 : 0);
        }
    }

    @Test
    @DisplayName("A tapped land cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new VectorImperialCapital()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }
}

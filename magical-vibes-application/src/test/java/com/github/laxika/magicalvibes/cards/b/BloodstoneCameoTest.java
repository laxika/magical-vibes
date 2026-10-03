package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BloodstoneCameo.class)
class BloodstoneCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Bloodstone Cameo adds one black mana")
    void tapForBlackMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new BloodstoneCameo());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(cameo.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Bloodstone Cameo adds one red mana")
    void tapForRedMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new BloodstoneCameo());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(cameo.isTapped()).isTrue();
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mana choice prevents another activation while the Cameo is tapped")
    void cannotActivateEitherChoiceAfterTapping(int firstAbilityIndex) {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new BloodstoneCameo());
        harness.activateAbility(player1, 0, firstAbilityIndex, null, null);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int choice = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, choice, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Permanent is already tapped");
        }

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK))
                .isEqualTo(firstAbilityIndex == 0 ? 1 : 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED))
                .isEqualTo(firstAbilityIndex == 1 ? 1 : 0);
    }

    @Test
    @DisplayName("Mana is added immediately to the activating controller's pool")
    void addsManaImmediatelyForSecondPlayer() {
        harness.addToBattlefieldAndReturn(player2, new BloodstoneCameo());

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}

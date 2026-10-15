package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VolcanicIsland.class)
class VolcanicIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Volcanic Island produces blue mana")
    void producesBlueMana() {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(volcanicIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Volcanic Island produces red mana")
    void producesRedMana() {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(volcanicIsland.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED"})
    void canProduceManaImmediatelyAfterBeingPlayed(ManaColor color) {
        harness.setHand(player1, List.of(new VolcanicIsland()));

        harness.playLand(player1, 0);

        Permanent volcanicIsland = findPermanent(player1, "Volcanic Island");
        assertThat(volcanicIsland.isTapped()).isFalse();

        harness.activateAbility(player1, 0, color == ManaColor.BLUE ? 0 : 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(volcanicIsland.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED"})
    void cannotActivateOtherManaAbilityWhileTapped(ManaColor color) {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());
        int abilityIndex = color == ManaColor.BLUE ? 0 : 1;

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(volcanicIsland.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

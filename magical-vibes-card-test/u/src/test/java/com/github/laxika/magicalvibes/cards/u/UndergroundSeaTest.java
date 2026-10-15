package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed(UndergroundSea.class)
class UndergroundSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Underground Sea produces blue mana")
    void producesBlueMana() {
        Permanent undergroundSea = addUndergroundSeaReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Underground Sea produces black mana")
    void producesBlackMana() {
        Permanent undergroundSea = addUndergroundSeaReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    void canProduceManaImmediatelyAfterBeingPlayed(ManaColor color) {
        harness.setHand(player1, List.of(new UndergroundSea()));

        harness.playLand(player1, 0);

        Permanent undergroundSea = findPermanent(player1, "Underground Sea");
        assertThat(undergroundSea.isTapped()).isFalse();

        harness.activateAbility(player1, 0, color == ManaColor.BLUE ? 0 : 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    void cannotProduceTheOtherColorWhileTapped(ManaColor color) {
        Permanent undergroundSea = harness.addToBattlefieldAndReturn(player1, new UndergroundSea());
        int abilityIndex = color == ManaColor.BLUE ? 0 : 1;
        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addUndergroundSeaReady() {
        Permanent undergroundSea = harness.addToBattlefieldAndReturn(player1, new UndergroundSea());
        undergroundSea.setSummoningSick(false);
        return undergroundSea;
    }
}

package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed(Tundra.class)
class TundraTest extends BaseCardTest {

    @Test
    @DisplayName("Tundra produces white mana")
    void producesWhiteMana() {
        Permanent tundra = addTundraReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tundra produces blue mana")
    void producesBlueMana() {
        Permanent tundra = addTundraReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
    }

    @Test
    void cannotActivateOtherManaAbilityWhileTapped() {
        Permanent tundra = addTundraReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(tundra.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    void canProduceManaImmediatelyAfterBeingPlayed(ManaColor color) {
        harness.setHand(player1, List.of(new Tundra()));

        harness.playLand(player1, 0);

        Permanent tundra = findPermanent(player1, "Tundra");
        assertThat(tundra.isTapped()).isFalse();

        harness.activateAbility(player1, 0, color == ManaColor.WHITE ? 0 : 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseTheOtherColorAfterUntapping() {
        Permanent tundra = harness.addToBattlefieldAndReturn(player1, new Tundra());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.performUntapStep(player1);
        assertThat(tundra.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tundra.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTundraReady() {
        Permanent tundra = harness.addToBattlefieldAndReturn(player1, new Tundra());
        tundra.setSummoningSick(false);
        return tundra;
    }
}

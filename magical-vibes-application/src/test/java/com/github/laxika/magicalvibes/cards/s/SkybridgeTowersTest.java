package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkybridgeTowers.class})
class SkybridgeTowersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SkybridgeTowers()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one white mana")
    void tappingAddsWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkybridgeTowers());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkybridgeTowers());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying two generic, one white, and one blue mana sacrifices the land and draws a card")
    void sacrificesAndDraws() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkybridgeTowers());
        SkybridgeTowers draw = new SkybridgeTowers();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Sacrifice and mana are paid immediately, but drawing waits for resolution")
    void paysCostsBeforeDrawing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkybridgeTowers());
        SkybridgeTowers draw = new SkybridgeTowers();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("A tapped land cannot activate any of its abilities")
    void cannotActivateWhenTapped(int abilityIndex) {
        harness.setHand(player1, List.of(new SkybridgeTowers()));
        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    @DisplayName("Four mana cannot pay the draw ability without both required colors")
    void requiresBothColors(ManaColor availableColor) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkybridgeTowers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, availableColor, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(availableColor)).isEqualTo(3);
    }
}

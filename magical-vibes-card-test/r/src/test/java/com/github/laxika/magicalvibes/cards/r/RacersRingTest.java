package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RacersRing.class})
class RacersRingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RacersRing()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one red mana")
    void tappingAddsRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RacersRing());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one green mana")
    void tappingAddsGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RacersRing());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying two generic, one red, and one green mana sacrifices the land and draws a card")
    void sacrificesAndDraws() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RacersRing());
        RacersRing draw = new RacersRing();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Sacrifice is paid on activation but drawing waits for resolution")
    void sacrificeIsACostAndDrawUsesTheStack() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RacersRing());
        RacersRing draw = new RacersRing();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("A tapped land cannot activate any of its abilities")
    void tappedLandCannotActivate(int abilityIndex) {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new RacersRing());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "RED", "GREEN"})
    @DisplayName("Drawing requires both colored mana and the full generic cost")
    void cannotDrawWithoutTheFullManaCost(ManaColor missingColor) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RacersRing());
        harness.addMana(player1, ManaColor.COLORLESS, missingColor == ManaColor.COLORLESS ? 1 : 3);
        harness.addMana(player1, ManaColor.RED, missingColor == ManaColor.RED ? 0 : 1);
        harness.addMana(player1, ManaColor.GREEN, missingColor == ManaColor.GREEN ? 0 : 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
        assertThat(gd.stack).isEmpty();
    }
}

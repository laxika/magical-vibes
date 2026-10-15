package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({FrostMarsh.class})
class FrostMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new FrostMarsh()));

        harness.playLand(player1, 0);

        Permanent frostMarsh = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(frostMarsh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent frostMarsh = addReadyFrostMarsh();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(frostMarsh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent frostMarsh = addReadyFrostMarsh();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(frostMarsh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent frostMarsh = harness.enterBattlefieldAndReturn(player1, new FrostMarsh());

        assertThat(frostMarsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot produce mana while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new FrostMarsh()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    @DisplayName("A newly controlled untapped land produces one snow mana immediately")
    void newlyControlledLandProducesSnowManaWithoutUsingStack(ManaColor color) {
        Permanent frostMarsh = harness.addToBattlefieldAndReturn(player1, new FrostMarsh());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(frostMarsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addReadyFrostMarsh() {
        return addCreatureReady(player1, new FrostMarsh());
    }
}

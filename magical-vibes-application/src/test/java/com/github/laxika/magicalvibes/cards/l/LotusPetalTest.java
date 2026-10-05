package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotusPetal.class})
class LotusPetalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating adds one mana of the chosen color and sacrifices itself")
    void activateAddsManaAndSacrifices() {
        harness.addToBattlefield(player1, new LotusPetal());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A different chosen color produces that color instead")
    void activateProducesChosenColor() {
        harness.addToBattlefield(player1, new LotusPetal());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Can produce exactly one mana of each of the five colors")
    void producesExactlyOneManaOfAnyColor(ManaColor chosenColor) {
        LotusPetal petal = new LotusPetal();
        harness.addToBattlefield(player1, petal);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(petal);
        harness.handleListChoice(player1, chosenColor.name());

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 1 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can activate immediately after entering the battlefield")
    void canActivateOnTheTurnItEnters() {
        LotusPetal petal = new LotusPetal();
        harness.castFromHand(player1, petal, "{0}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lotus Petal");

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(petal);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent petal = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        petal.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(petal);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}

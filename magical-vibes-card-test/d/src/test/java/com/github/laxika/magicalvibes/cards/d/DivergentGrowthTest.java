package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivergentGrowth.class, TempleOfTheFalseGod.class})
class DivergentGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control can tap for one mana of any color until end of turn")
    void landsCanTapForAnyColor() {
        var temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Divergent Growth does not grant the ability to an opponent's lands")
    void doesNotGrantAbilityToOpponentsLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.addToBattlefield(player2, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.passUntil(TurnStep.CLEANUP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land entering after resolution does not gain the temporary ability")
    void landsEnteringAfterResolutionDoNotGainAbility() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.addToBattlefield(player1, new TempleOfTheFalseGod());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .as("newly entered lands are not affected by the resolving spell")
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The granted ability produces exactly one mana of each available color")
    void grantedAbilityCanProduceEachColor(ManaColor color) {
        var temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Every land present at resolution receives the ability")
    void grantsAbilityToMultipleLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        castDivergentGrowth();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted ability does not replace the land's original ability")
    void preservesOriginalManaAbility() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        }
        castDivergentGrowth();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void castDivergentGrowth() {
        harness.castFromHand(player1, new DivergentGrowth(), "{G}");
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CalderaLake.class)
class CalderaLakeTest extends BaseCardTest {

    @Test
    @DisplayName("Caldera Lake enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new CalderaLake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Caldera Lake").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent lake = addCreatureReady(player1, new CalderaLake());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(lake.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and deals 1 damage to controller")
    void tapForBlueAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new CalderaLake());

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for red adds {R} and deals 1 damage to controller")
    void tapForRedAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new CalderaLake());

        harness.activateAbility(player1, 0, 2, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Caldera Lake cannot activate any mana ability")
    void tappedLakeCannotProduceMana() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new CalderaLake());
        lake.tap();
        harness.setLife(player1, 20);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Caldera Lake damages only its controller")
    void opponentLakeDamagesItsController() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lake = addCreatureReady(player2, new CalderaLake());

        harness.activateAbility(player2, 0, 1, null, null);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(lake.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped noncreature Caldera Lake can produce mana immediately")
    void noncreatureLakeIgnoresSummoningSickness() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new CalderaLake());
        lake.setSummoningSick(true);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, null);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(lake.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

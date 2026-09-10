package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed(PineBarrens.class)
class PineBarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Pine Barrens enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new PineBarrens()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Pine Barrens").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent land = addCreatureReady(player1, new PineBarrens());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(land.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and deals 1 damage to controller")
    void tapForBlackAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new PineBarrens());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for green adds {G} and deals 1 damage to controller")
    void tapForGreenAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new PineBarrens());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana abilities damage only their controller")
    void coloredManaDamagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        addCreatureReady(player1, new PineBarrens());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate a mana ability while Pine Barrens is tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new PineBarrens());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}

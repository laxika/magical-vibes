package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MirrorUniverse.class)
class MirrorUniverseTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals with an opponent and sacrifices itself")
    void exchangesLifeTotalsAndSacrificesItself() {
        addReadyMirror(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Mirror Universe");
        harness.assertInGraveyard(player1, "Mirror Universe");
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        addReadyMirror(player1);
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Can only be activated during its controller's upkeep")
    void onlyActivatesDuringControllersUpkeep() {
        addReadyMirror(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MirrorUniverse);
    }

    private Permanent addReadyMirror(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new MirrorUniverse());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

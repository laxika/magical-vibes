package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LethalVapors.class, GrizzlyBears.class})
class LethalVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every creature as it enters")
    void destroysEnteringCreature() {
        harness.addToBattlefield(player1, new LethalVapors());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Any player may destroy the enchantment and skip their next turn")
    void anyPlayerMayDestroyItAndSkipTheirNextTurn() {
        Permanent vapors = harness.addToBattlefieldAndReturn(player1, new LethalVapors());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lethal Vapors");
        assertThat(vapors).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();

        advanceTurn(player2);
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void advanceTurn(Player expectedActivePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(expectedActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}

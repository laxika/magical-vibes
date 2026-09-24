package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoonOfTheHiddenRealm.class, GrizzlyBears.class, Quicksand.class})
class RoonOfTheHiddenRealmTest extends BaseCardTest {

    private Permanent addReadyRoon(Player player) {
        Permanent roon = new Permanent(new RoonOfTheHiddenRealm());
        roon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(roon);
        return roon;
    }

    private void addRoonMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exiles another target creature and returns it at the next end step")
    void exilesAndReturnsAnotherCreature() {
        addReadyRoon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addRoonMana(player1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(action -> action.card().getName().equals("Grizzly Bears"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target Roon itself")
    void cannotTargetItself() {
        Permanent roon = addReadyRoon(player1);
        addRoonMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, roon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyRoon(player1);
        harness.addToBattlefield(player2, new Quicksand());
        addRoonMana(player1);

        UUID quicksandId = harness.getPermanentId(player2, "Quicksand");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, quicksandId))
                .isInstanceOf(IllegalStateException.class);
    }
}

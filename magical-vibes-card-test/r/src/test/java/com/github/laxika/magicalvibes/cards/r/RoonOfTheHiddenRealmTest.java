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
        Permanent roon = harness.addToBattlefieldAndReturn(player, new RoonOfTheHiddenRealm());
        roon.setSummoningSick(false);
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
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void returnsStolenCreatureToOwnerAfterRoonLeaves() {
        Permanent roon = addReadyRoon(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        addRoonMana(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        assertThat(roon.isTapped()).isTrue();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(roon);
        gd.playerGraveyards.get(player1.getId()).add(roon.getCard());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void activationDuringEndStepWaitsForFollowingEndStep() {
        addReadyRoon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.END_STEP);
        addRoonMana(player1);
        harness.activateAbility(player1, 0, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotReturnCardThatLeftExileAndWasExiledAgainBeforeEndStep() {
        addReadyRoon(player1);
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);
        addRoonMana(player1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.removeFromExile(bears.getId())).isTrue();
        gd.playerGraveyards.get(player2.getId()).add(bears);
        gd.playerGraveyards.get(player2.getId()).remove(bears);
        gd.addToExile(player2.getId(), bears);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getId()));
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RoonOfTheHiddenRealm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addRoonMana(player1);
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        addReadyRoon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        addReadyRoon(player1).tap();
        harness.addToBattlefield(player2, new GrizzlyBears());
        addRoonMana(player1);
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
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

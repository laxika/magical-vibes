package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TinybonesBaubleBurglarTest extends BaseCardTest {

    @Test
    void exilesAnOpponentsDiscardWithAStashCounterAndLetsControllerCastIt() {
        Card discarded = new GrizzlyBears();
        harness.addToBattlefield(player1, new TinybonesBaubleBurglar());
        harness.setHand(player2, List.of(discarded));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);

        harness.addMana(player1, ManaColor.WHITE, 2);
        gs.playCardFromExile(gd, player1, discarded.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void stashPermissionSurvivesTheTinybonesThatCreatedItLeavingTheBattlefield() {
        Card discarded = new GrizzlyBears();
        TinybonesBaubleBurglar tinybones = new TinybonesBaubleBurglar();
        harness.addToBattlefield(player1, tinybones);
        UUID tinybonesPermanentId = harness.getPermanentId(player1, "Tinybones, Bauble Burglar");
        harness.setHand(player2, List.of(discarded));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, tinybonesPermanentId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tinybones, Bauble Burglar");

        harness.addToBattlefield(player1, new TinybonesBaubleBurglar());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);
        gs.playCardFromExile(gd, player1, discarded.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

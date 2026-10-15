package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.o.OkoThiefOfCrowns;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginsNexus.class, CaptureOfJingzhou.class, KuldothaRebirth.class, OkoThiefOfCrowns.class})
class UginsNexusTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("An extra turn is skipped while Ugin's Nexus remains on the battlefield")
    void skipsExtraTurnWhileOnBattlefield() {
        harness.addToBattlefield(player1, new UginsNexus());

        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, 0);

        int turnBefore = gd.turnNumber;
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Ugin's Nexus exiles it and queues an extra turn")
    void sacrificingNexusExilesItAndQueuesExtraTurn() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new UginsNexus());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorceryWithSacrifice(player1, 0, nexus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nexus.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(nexus.getCard().getId()));
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void skipsOpponentsExtraTurn() {
        harness.addToBattlefield(player1, new UginsNexus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void queuedExtraTurnsAreTakenIfNexusLeavesBeforeTheyBegin() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new UginsNexus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CaptureOfJingzhou(), new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.castSorceryWithSacrifice(player1, 0, nexus.getId());
        harness.passBothPriorities();
        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    void nexusWithNoAbilitiesDoesNotSkipExtraTurns() {
        harness.addToBattlefield(player1, new OkoThiefOfCrowns());
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new UginsNexus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, nexus.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void nexusWithNoAbilitiesDiesNormallyWithoutGrantingAnExtraTurn() {
        harness.addToBattlefield(player1, new OkoThiefOfCrowns());
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new UginsNexus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, nexus.getId());
        harness.passBothPriorities();

        nexus.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nexus.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(nexus.getCard());
        assertThat(gd.extraTurns).isEmpty();
    }
}

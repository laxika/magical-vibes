package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueenMarchesa.class, GrizzlyBears.class})
class QueenMarchesaTest extends BaseCardTest {

    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void createsAnAssassinDuringUpkeepWhenAnOpponentIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new QueenMarchesa());

        advanceToQueenUpkeep(player1);
        harness.passBothPriorities();

        Permanent assassin = findPermanent(player1, "Assassin");
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotCreateAnAssassinWhenItsControllerIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();

        advanceToQueenUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
    }

    @Test
    void combatDamageToTheMonarchTransfersTheMonarchDesignation() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();
        findPermanent(player1, "Queen Marchesa").tap();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        int bearIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bear);
        declareAttackers(player2, List.of(bearIndex));
        resolveCombat(player2);

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    private void advanceToQueenUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}

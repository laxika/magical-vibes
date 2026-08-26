package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NorinTheWary.class, GrizzlyBears.class})
class NorinTheWaryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles when any player casts a spell and returns at the next end step")
    void exilesWhenAnyPlayerCastsSpell() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertExiled(norin);

        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertReturned(norin);
    }

    @Test
    @DisplayName("Exiles when any creature attacks and returns at the next end step")
    void exilesWhenAnyCreatureAttacks() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        assertExiled(norin);

        advanceToEndStep(player2);

        assertReturned(norin);
    }

    private void assertExiled(Permanent norin) {
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(norin);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(norin.getCard().getId()));
    }

    private void assertReturned(Permanent norin) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(norin.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(norin.getCard().getId()));
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}

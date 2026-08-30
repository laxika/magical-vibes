package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Arboria.class, GrizzlyBears.class})
class ArboriaTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures can't attack a player who did not act during their last turn")
    void cannotAttackPlayerWhoDidNotActDuringLastTurn() {
        harness.addToBattlefield(player1, new Arboria());
        addCreatureReady(player1, new GrizzlyBears());

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A player who cast a spell during their last turn can be attacked")
    void canAttackPlayerWhoCastSpellDuringLastTurn() {
        harness.addToBattlefield(player1, new Arboria());
        addCreatureReady(player1, new GrizzlyBears());
        gd.activePlayerId = player2.getId();
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        gd.snapshotPlayerActionsForLastTurn(player2.getId());

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(1));
    }

    @Test
    @DisplayName("Arboria does not restrict attacks against planeswalkers")
    void doesNotRestrictAttacksAgainstPlaneswalkers() {
        harness.addToBattlefield(player2, new Arboria());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }

    @Test
    @DisplayName("Putting a nontoken permanent onto the battlefield during a turn qualifies")
    void puttingNontokenPermanentOntoBattlefieldQualifies() {
        harness.addToBattlefield(player1, new Arboria());
        addCreatureReady(player1, new GrizzlyBears());
        gd.activePlayerId = player2.getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.snapshotPlayerActionsForLastTurn(player2.getId());

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(1));
    }

    @Test
    @DisplayName("Putting a token onto the battlefield does not qualify")
    void puttingTokenOntoBattlefieldDoesNotQualify() {
        harness.addToBattlefield(player1, new Arboria());
        addCreatureReady(player1, new GrizzlyBears());
        Card token = new Card();
        token.setName("Test Token");
        token.setType(CardType.CREATURE);
        token.setToken(true);
        gd.activePlayerId = player2.getId();
        harness.addToBattlefield(player2, token);
        gd.snapshotPlayerActionsForLastTurn(player2.getId());

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

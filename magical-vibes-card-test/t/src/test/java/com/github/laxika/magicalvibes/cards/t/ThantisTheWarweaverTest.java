package com.github.laxika.magicalvibes.cards.t;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThantisTheWarweaver.class, GrizzlyBears.class})
class ThantisTheWarweaverTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures must attack each combat when able")
    void allCreaturesMustAttack() {
        harness.addToBattlefield(player1, new ThantisTheWarweaver());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Gets a counter for each creature attacking you")
    void getsCounterForEachCreatureAttackingYou() {
        Permanent thantis = addCreatureReady(player1, new ThantisTheWarweaver());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(thantis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers when a creature attacks a planeswalker you control")
    void triggersWhenCreatureAttacksPlaneswalker() {
        Permanent thantis = addCreatureReady(player1, new ThantisTheWarweaver());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player2, List.of(attackerIndex), Map.of(attackerIndex, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(thantis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

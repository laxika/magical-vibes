package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GahijiHonoredOne.class, GrizzlyBears.class})
class GahijiHonoredOneTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking an opponent gets +2/+0 until end of turn")
    void boostsCreatureAttackingOpponent() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A creature attacking an opponent's planeswalker gets +2/+0")
    void boostsCreatureAttackingOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackerAtPermanent(player1, attacker, planeswalker);
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking the controller or the controller's planeswalker does not trigger")
    void doesNotBoostAttacksAgainstController() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent planeswalker = addPlaneswalker(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackerAtPermanent(player2, attacker, planeswalker);

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private void declareAttackerAtPermanent(Player attackerController, Permanent attacker, Permanent target) {
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackerController, List.of(attackerIndex), Map.of(attackerIndex, target.getId()));
    }
}

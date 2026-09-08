package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage at upkeep to an opponent it previously damaged")
    void damagesPreviouslyDamagedOpponentAtUpkeep() {
        Permanent fallen = addCreatureReady(player1, new TheFallen());
        fallen.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not deal damage at upkeep before damaging a recipient")
    void doesNotDamageWithoutPreviousDamage() {
        harness.addToBattlefield(player1, new TheFallen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 1 damage at upkeep to a planeswalker it previously damaged")
    void damagesPreviouslyDamagedPlaneswalkerAtUpkeep() {
        Permanent fallen = addCreatureReady(player1, new TheFallen());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        fallen.setAttackTarget(planeswalker.getId());
        fallen.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}");
        card.setColor(CardColor.BLUE);

        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}

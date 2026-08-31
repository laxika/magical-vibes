package com.github.laxika.magicalvibes.cards.t;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TheFallen.class)
class TheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage on upkeep to an opponent it damaged earlier in the game")
    void damagesPreviouslyDamagedOpponentOnUpkeep() {
        addCreatureReady(player1, new TheFallen());

        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        advanceToNextUpkeep(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 1 damage on upkeep to a planeswalker it damaged earlier in the game")
    void damagesPreviouslyDamagedPlaneswalkerOnUpkeep() {
        addCreatureReady(player1, new TheFallen());
        Permanent planeswalker = addPlaneswalker(player2, 5);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveCombat();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

        advanceToNextUpkeep(player1);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void forgetsRecipientsWhenItLeavesBeforeUpkeepTriggerResolves() {
        Permanent fallen = addCreatureReady(player1, new TheFallen());

        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.inMutationScope(new Runnable() {
            @Override
            public void run() {
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, fallen);
            }
        });
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not damage an opponent or planeswalker it has not damaged")
    void doesNotDamageUnmarkedRecipients() {
        addCreatureReady(player1, new TheFallen());
        Permanent planeswalker = addPlaneswalker(player2, 5);

        advanceToNextUpkeep(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private void advanceToNextUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
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

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}

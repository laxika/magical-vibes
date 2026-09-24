package com.github.laxika.magicalvibes.cards.r;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riddlekeeper.class, GrizzlyBears.class})
class RiddlekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking you makes the attacker's controller mill two cards")
    void attackingPlayerMillsTwo() {
        addRiddlekeeper(player1);
        addCreatureReady(player2, new GrizzlyBears());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0), null);
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Attacking your planeswalker also makes the attacker's controller mill two cards")
    void attackingPlaneswalkerMillsTwo() {
        addRiddlekeeper(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addCreatureReady(player2, new GrizzlyBears());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability triggers once for each attacking creature")
    void triggersOncePerAttackingCreature() {
        addRiddlekeeper(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        setDeck(player2, 6);

        declareAttackers(player2, List.of(0, 1), null);
        resolveTopTrigger();
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private void addRiddlekeeper(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Riddlekeeper()));
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setDeck(Player player, int size) {
        gd.playerDecks.get(player.getId()).clear();
        for (int i = 0; i < size; i++) {
            gd.playerDecks.get(player.getId()).add(new GrizzlyBears());
        }
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void resolveTopTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}

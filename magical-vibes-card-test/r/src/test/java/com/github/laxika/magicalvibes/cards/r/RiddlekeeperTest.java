package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.h.HornetQueen;
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

@CardUsed({Riddlekeeper.class, HornetQueen.class, GarrukWildspeaker.class})
class RiddlekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking you makes the attacker's controller mill two cards")
    void attackingPlayerMillsTwo() {
        addRiddlekeeper(player1);
        addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Attacking your planeswalker also makes the attacker's controller mill two cards")
    void attackingPlaneswalkerMillsTwo() {
        addRiddlekeeper(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability triggers once for each attacking creature")
    void triggersOncePerAttackingCreature() {
        addRiddlekeeper(player1);
        addCreatureReady(player2, new HornetQueen());
        addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 6);

        declareAttackers(player2, List.of(0, 1));
        resolveTopTrigger();
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The attacker still mills after the attacking creature leaves the battlefield")
    void millsAfterAttackerLeavesBattlefield() {
        addRiddlekeeper(player1);
        Permanent attacker = addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, attacker));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Removing Riddlekeeper does not stop its already triggered ability")
    void millsAfterRiddlekeeperLeavesBattlefield() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new Riddlekeeper());
        addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 4);

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, keeper));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A short library mills only the available cards")
    void millsRemainingCardFromShortLibrary() {
        addRiddlekeeper(player1);
        addCreatureReady(player2, new HornetQueen());
        setDeck(player2, 1);

        declareAttackers(player2, List.of(0));
        resolveTopTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking with your own creature does not trigger your Riddlekeeper")
    void doesNotTriggerForOwnAttack() {
        addRiddlekeeper(player1);
        addCreatureReady(player1, new HornetQueen());
        setDeck(player1, 4);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void addRiddlekeeper(Player player) {
        harness.addToBattlefield(player, new Riddlekeeper());
    }

    private void setDeck(Player player, int size) {
        harness.setLibrary(player, java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> new HornetQueen()).toList());
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

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TroubleInPairs.class, CaptureOfJingzhou.class, Forest.class, GrizzlyBears.class,
        JaceBeleren.class, LightningBolt.class})
class TroubleInPairsTest extends BaseCardTest {

    @Test
    @DisplayName("Skips an opponent's extra turn")
    void skipsOpponentExtraTurn() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player2, List.of(new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player2.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Does not skip the controller's extra turn")
    void doesNotSkipControllerExtraTurn() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("Draws when an opponent attacks with two creatures")
    void drawsWhenOpponentAttacksWithTwoCreatures() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the creatures attack a planeswalker")
    void doesNotDrawWhenCreaturesAttackPlaneswalker() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0, 1), Map.of(0, planeswalker.getId(), 1, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws when an opponent draws their second card")
    void drawsWhenOpponentDrawsSecondCard() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2.getId());
        draw(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an opponent casts their second spell")
    void drawsWhenOpponentCastsSecondSpell() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("One creature attacking you does not trigger")
    void oneAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One creature attacking you and one attacking your planeswalker do not trigger")
    void splitAttackDoesNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three attacking creatures produce just one draw")
    void threeAttackersDrawOnce() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(player2, List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two creatures attacking in each of two combats trigger twice")
    void repeatedAttacksTriggerAgain() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        first.untap();
        second.untap();
        first.setAttacking(false);
        second.setAttacking(false);
        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only the second opponent draw triggers, even if a third draw precedes resolution")
    void firstAndThirdDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        draw(player2.getId());
        assertThat(gd.stack).isEmpty();
        draw(player2.getId());
        assertThat(gd.stack).hasSize(1);
        draw(player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller's second draw does not trigger")
    void controllerDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        draw(player1.getId());
        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts the opponent's first draw before Trouble in Pairs enters")
    void countsEarlierDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        draw(player2.getId());
        harness.addToBattlefield(player1, new TroubleInPairs());

        draw(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only the second opponent spell triggers, before either spell resolves")
    void secondSpellTriggersBeforeResolution() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts the opponent's first spell before Trouble in Pairs enters")
    void countsEarlierSpell() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TroubleInPairs());

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller's second spell does not trigger")
    void controllerSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draw and spell events can both trigger during the same turn")
    void differentEventsTriggerIndependently() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player2.getId());
        draw(player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An extra turn is unaffected if Trouble in Pairs leaves before it begins")
    void removedEnchantmentDoesNotSkipExtraTurn() {
        Permanent trouble = harness.addToBattlefieldAndReturn(player1, new TroubleInPairs());
        harness.setHand(player2, List.of(new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player2, 0, 0);
        gd.playerBattlefields.get(player1.getId()).remove(trouble);
        gd.playerGraveyards.get(player1.getId()).add(trouble.getCard());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
    }

    @Test
    @DisplayName("The controller attacking with two creatures does not trigger")
    void controllerAttacksDoNotTrigger() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second-draw trigger resets on the next turn")
    void drawCountResetsEachTurn() {
        harness.addToBattlefield(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        draw(player2.getId());
        draw(player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        draw(player2.getId());
        assertThat(gd.stack).isEmpty();
        draw(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A queued draw trigger survives Trouble in Pairs leaving the battlefield")
    void queuedDrawSurvivesSourceRemoval() {
        Permanent trouble = harness.addToBattlefieldAndReturn(player1, new TroubleInPairs());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        draw(player2.getId());
        draw(player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trouble);
        gd.playerGraveyards.get(player1.getId()).add(trouble.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private void declareAttackers(com.github.laxika.magicalvibes.model.Player player,
                                  List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}

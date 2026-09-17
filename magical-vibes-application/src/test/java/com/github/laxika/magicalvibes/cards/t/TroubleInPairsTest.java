package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({TroubleInPairs.class, CaptureOfJingzhou.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
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
        Permanent planeswalker = addPlaneswalker(player1);
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

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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

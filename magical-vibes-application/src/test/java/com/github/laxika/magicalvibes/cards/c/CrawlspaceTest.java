package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlspaceTest extends BaseCardTest {

    @Test
    @DisplayName("No more than two creatures can attack its controller")
    void limitsAttacksAgainstController() {
        addReadyPermanent(player2, new Crawlspace());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1, 2), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 2 creatures can attack");
    }

    @Test
    @DisplayName("Two creatures can attack its controller")
    void allowsTwoAttacksAgainstController() {
        addReadyPermanent(player2, new Crawlspace());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1), null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not limit creatures attacking the controller's planeswalker")
    void doesNotLimitAttacksAgainstPlaneswalker() {
        addReadyPermanent(player2, new Crawlspace());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        Map<Integer, UUID> targets = Map.of(
                0, planeswalker.getId(),
                1, planeswalker.getId(),
                2, planeswalker.getId());
        assertThatCode(() -> declareAttackers(player1, List.of(0, 1, 2), targets))
                .doesNotThrowAnyException();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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

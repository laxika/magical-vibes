package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AriusFlybyTrawler.class, Forest.class, GrizzlyBears.class, MindRot.class})
class AriusFlybyTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks a non-Shark card and discards that exact card at the next end step")
    void seeksAndDiscardsTheSelectedCard() {
        Permanent arius = addCreatureReady(player1, new AriusFlybyTrawler());
        Card extra = new Forest();
        Card sought = new GrizzlyBears();
        Card shark = new AriusFlybyTrawler();
        harness.setHand(player1, List.of(extra));
        harness.setLibrary(player1, List.of(shark, sought));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shark);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra, sought);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra);
        assertThat(arius.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not seek or register a discard when the library has only Sharks")
    void doesNothingWithoutANonSharkCard() {
        addCreatureReady(player1, new AriusFlybyTrawler());
        Card shark = new AriusFlybyTrawler();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shark));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shark);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Puts only one counter on a two-card discard event")
    void putsOneCounterForDiscardEvent() {
        Permanent arius = addCreatureReady(player1, new AriusFlybyTrawler());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new MindRot()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(arius.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting the sought card prevents its delayed discard without discarding a replacement")
    void doesNotDiscardAnotherCardWhenSoughtCardIsCast() {
        Permanent arius = addCreatureReady(player1, new AriusFlybyTrawler());
        Card extra = new Forest();
        Card sought = new GrizzlyBears();
        harness.setHand(player1, List.of(extra));
        harness.setLibrary(player1, List.of(sought));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == sought);
        assertThat(arius.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent discarding cards does not put a counter on Arius")
    void doesNotTriggerForOpponentDiscard() {
        Permanent arius = addCreatureReady(player1, new AriusFlybyTrawler());
        harness.setHand(player1, List.of(new MindRot()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(arius.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Magmaquake;
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

@CardUsed({ThoughtSponge.class, Forest.class, Magmaquake.class})
class ThoughtSpongeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the greatest number of cards an opponent drew this turn")
    void entersWithGreatestOpponentDrawCount() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 7);
        gd.cardsDrawnThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new ThoughtSponge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sponge = findPermanent(player1, "Thought Sponge");
        assertThat(sponge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(sponge.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Draws cards equal to its last-known power when it dies")
    void deathTriggerDrawsItsPower() {
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ThoughtSponge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sponge = findPermanent(player1, "Thought Sponge");
        assertThat(sponge.getEffectivePower()).isEqualTo(3);

        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Thought Sponge");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Enters without counters when only its controller has drawn cards")
    void entersWithoutCountersWhenOpponentHasNotDrawn() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 7);
        harness.setHand(player1, List.of(new ThoughtSponge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thought Sponge")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast during the opponent's upkeep and checks draw count on entry")
    void flashChecksDrawCountAtResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.getDrawService().resolveDrawCard(gd, player2.getId());
        harness.setHand(player1, List.of(new ThoughtSponge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.getDrawService().resolveDrawCards(gd, player2.getId(), 3);
        harness.passBothPriorities();

        Permanent sponge = findPermanent(player1, "Thought Sponge");
        assertThat(sponge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.getDrawService().resolveDrawCards(gd, player2.getId(), 2);
        assertThat(sponge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A sponge with no entry counters still draws one card when it dies")
    void deathWithoutEntryCountersDrawsOne() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ThoughtSponge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Thought Sponge");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}

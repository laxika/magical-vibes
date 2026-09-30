package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtSponge.class, Forest.class, LightningBolt.class})
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

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sponge.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Thought Sponge");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }
}

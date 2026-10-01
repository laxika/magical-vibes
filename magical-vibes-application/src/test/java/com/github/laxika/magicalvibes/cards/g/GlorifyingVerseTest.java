package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlorifyingVerse.class, HillGiant.class, Shock.class})
class GlorifyingVerseTest extends BaseCardTest {

    @Test
    void reparteeConjuresGlorifyingVerseWhenCreatureIsTargeted() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorifying Verse");
    }

    @Test
    void reparteeDoesNotTriggerWhenPlayerIsTargeted() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

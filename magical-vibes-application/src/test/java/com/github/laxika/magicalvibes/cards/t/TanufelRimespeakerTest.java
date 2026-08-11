package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TanufelRimespeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater draws a card")
    void highManaValueSpellDrawsCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not draw a card")
    void lowManaValueSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

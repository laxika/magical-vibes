package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeaveFateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when it resolves")
    void drawsTwoCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player1, List.of(new WeaveFate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Lightning Bolt");
        harness.assertInGraveyard(player1, "Weave Fate");
    }
}

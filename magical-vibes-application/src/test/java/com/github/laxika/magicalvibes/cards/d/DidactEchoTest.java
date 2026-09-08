package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DidactEcho.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class DidactEchoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setHand(player1, List.of(new DidactEcho()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Has flying with four permanent cards in its controller's graveyard")
    void hasFlyingAtFourPermanentCards() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Island(), new Forest()));

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Nonpermanent cards do not count toward flying")
    void nonpermanentCardsDoNotCount() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new DidactEcho());
        List<Card> graveyard = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Island(), new Shock()));

        assertThat(gqs.hasKeyword(gd, echo, Keyword.FLYING)).isFalse();
    }
}

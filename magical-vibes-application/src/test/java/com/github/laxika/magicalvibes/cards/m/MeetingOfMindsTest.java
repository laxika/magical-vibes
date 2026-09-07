package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeetingOfMinds.class, GrizzlyBears.class})
class MeetingOfMindsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Meeting of Minds draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MeetingOfMinds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Convoke taps creatures to pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeetingOfMinds()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TidalTerror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeetingOfMinds.class, GrizzlyBears.class, TidalTerror.class})
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

    @Test
    @DisplayName("A summoning-sick blue creature can convoke the blue mana cost")
    void blueCreaturePaysColoredCostWhileSummoningSick() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TidalTerror());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new MeetingOfMinds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Meeting of Minds");
    }

    @Test
    @DisplayName("Four blue creatures can convoke the entire cost without mana")
    void castsUsingOnlyConvoke() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TidalTerror());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TidalTerror());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new TidalTerror());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new TidalTerror());
        harness.setHand(player1, List.of(new MeetingOfMinds()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(List.of(first, second, third, fourth)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}

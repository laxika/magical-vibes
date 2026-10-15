package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanYeti.class, BearCub.class, Forest.class})
class SylvanYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in controller's hand; toughness stays 4")
    void powerEqualsHandSize() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new BearCub(), new BearCub(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power is 0 with an empty hand; toughness stays 4")
    void powerZeroWithEmptyHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates dynamically as hand size changes")
    void powerUpdatesDynamically() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of());

        harness.setHand(player1, List.of(new BearCub()));
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);

        harness.setHand(player1, List.of(new BearCub(), new BearCub()));
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new BearCub()));
        harness.setHand(player2, List.of(new BearCub(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power counts noncreature cards in controller's hand")
    void countsNoncreatureCardsInHand() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new Forest(), new BearCub()));

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Yeti as the last card in hand produces a surviving 0/4")
    void castingLastCardLeavesZeroPowerCreature() {
        harness.castFromHand(player1, new SylvanYeti(), "{2}{G}{G}");
        harness.passBothPriorities();

        Permanent yeti = findPermanent(player1, "Sylvan Yeti");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, yeti)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power counts Yeti itself while it is in its owner's hand")
    void definesPowerInHand() {
        SylvanYeti yeti = new SylvanYeti();
        harness.setHand(player1, List.of(yeti, new Forest()));
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, yeti)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power in the graveyard tracks its owner's hand")
    void definesPowerInGraveyard() {
        SylvanYeti yeti = new SylvanYeti();
        harness.setGraveyard(player1, List.of(yeti));
        harness.setHand(player1, List.of(new Forest(), new BearCub()));
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, yeti)).isEqualTo(2);
        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectiveCardPower(gd, yeti)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, yeti)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters modify the hand-defined power and printed toughness")
    void countersApplyAfterHandDefinedPower() {
        Permanent yeti = addCreatureReady(player1, new SylvanYeti());
        harness.setHand(player1, List.of(new BearCub(), new Forest()));
        yeti.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(5);
        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(5);
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurChieftain.class, BaskingRootwalla.class})
class CentaurChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB boosts and grants trample to your creatures, including itself")
    void thresholdEtbBoostsAndGrantsTrample() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        Permanent opponentRootwalla = harness.addToBattlefieldAndReturn(player2, new BaskingRootwalla());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        Permanent chieftain = castChieftain();

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rootwalla)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rootwalla, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentRootwalla)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentRootwalla)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentRootwalla, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Below threshold, the ETB ability is not granted")
    void belowThresholdDoesNotTrigger() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, graveyardWithSixCards());

        Permanent chieftain = castChieftain();

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rootwalla)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rootwalla, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The threshold trigger uses the entry-time threshold and affects creatures at resolution")
    void thresholdTriggerUsesEntryStateAndCurrentCreatures() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        harness.castFromHand(player1, new CentaurChieftain(), "{3}{G}");
        harness.passBothPriorities();
        Permanent chieftain = findPermanent(player1, "Centaur Chieftain");

        Permanent lateRootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, graveyardWithSixCards());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rootwalla)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rootwalla, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lateRootwalla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateRootwalla)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateRootwalla, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The temporary boost and trample grant wear off at cleanup")
    void effectsWearOffAtEndOfTurn() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        castChieftain();

        assertThat(gqs.hasKeyword(gd, rootwalla, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rootwalla)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rootwalla)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rootwalla, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castChieftain() {
        harness.castFromHand(player1, new CentaurChieftain(), "{3}{G}");
        harness.passBothPriorities();
        Permanent chieftain = findPermanent(player1, "Centaur Chieftain");
        harness.passBothPriorities();
        return chieftain;
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new BaskingRootwalla(), new BaskingRootwalla(), new BaskingRootwalla(),
                new BaskingRootwalla(), new BaskingRootwalla(), new BaskingRootwalla(),
                new BaskingRootwalla());
    }

    private List<Card> graveyardWithSixCards() {
        return List.of(
                new BaskingRootwalla(), new BaskingRootwalla(), new BaskingRootwalla(),
                new BaskingRootwalla(), new BaskingRootwalla(), new BaskingRootwalla());
    }
}

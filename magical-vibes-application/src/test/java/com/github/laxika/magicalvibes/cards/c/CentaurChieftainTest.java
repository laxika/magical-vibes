package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurChieftain.class, GrizzlyBears.class})
class CentaurChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB boosts and grants trample to your creatures, including itself")
    void thresholdEtbBoostsAndGrantsTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        Permanent chieftain = castChieftain();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Below threshold, the ETB ability is not granted")
    void belowThresholdDoesNotTrigger() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent chieftain = castChieftain();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and trample grant wear off at cleanup")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        castChieftain();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castChieftain() {
        harness.setHand(player1, List.of(new CentaurChieftain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent chieftain = findPermanent(player1, "Centaur Chieftain");
        harness.passBothPriorities();
        return chieftain;
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
    }
}

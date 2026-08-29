package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeetleLegacyCriminal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocOckSinisterScientist.class, BeetleLegacyCriminal.class, GrizzlyBears.class})
class DocOckSinisterScientistTest extends BaseCardTest {

    @Test
    @DisplayName("Has base power and toughness 8/8 with eight cards in its controller's graveyard")
    void getsEightEightWithEightCardsInGraveyard() {
        harness.setGraveyard(player1, graveyardWithEightCards());
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());

        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(8);
    }

    @Test
    @DisplayName("Loses the base power and toughness bonus below eight cards in its controller's graveyard")
    void losesEightEightWhenGraveyardDropsBelowEightCards() {
        harness.setGraveyard(player1, graveyardWithEightCards());
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gains hexproof while its controller controls another Villain")
    void gainsHexproofWithAnotherVillain() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new BeetleLegacyCriminal());

        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not gain hexproof from itself or an opponent's Villain")
    void requiresAnotherVillainYouControl() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());

        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isFalse();

        harness.addToBattlefield(player2, new BeetleLegacyCriminal());

        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isFalse();
    }

    private List<Card> graveyardWithEightCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}

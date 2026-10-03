package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeetleLegacyCriminal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Updates base stats when the controller's graveyard crosses eight cards")
    void updatesAtGraveyardThreshold() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        List<Card> cards = List.of(
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal());

        harness.setGraveyard(player1, cards.subList(0, 7));
        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(5);

        harness.setGraveyard(player1, cards);
        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(8);

        harness.setGraveyard(player1, cards.subList(0, 7));
        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count the opponent's graveyard toward the eight-card threshold")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal()));
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());

        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Loses hexproof when the other controlled Villain leaves the battlefield")
    void losesHexproofWhenOtherVillainLeaves() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new BeetleLegacyCriminal());
        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, beetle));

        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isFalse();
    }
    @Test
    @DisplayName("Hexproof allows its controller's ability and counters modify the 8/8 base stats")
    void allowsOwnTargetingAndAppliesCountersAboveBaseStats() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new BeetleLegacyCriminal());
        harness.setGraveyard(player1, List.of(
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal(), new BeetleLegacyCriminal(),
                new BeetleLegacyCriminal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, docOck.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, docOck)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, docOck)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, docOck, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent's targeted ability")
    void rejectsOpponentsTargetedAbility() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new BeetleLegacyCriminal());
        harness.setGraveyard(player2, List.of(new BeetleLegacyCriminal()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0, docOck.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Beetle, Legacy Criminal");
    }
    private List<Card> graveyardWithEightCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}

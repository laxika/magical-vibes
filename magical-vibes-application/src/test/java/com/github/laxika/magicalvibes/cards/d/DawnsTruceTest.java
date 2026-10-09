package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DawnsTruce.class, GrizzlyBears.class, Plains.class, Shock.class})
class DawnsTruceTest extends BaseCardTest {

    @Test
    @DisplayName("Dawn's Truce grants hexproof to you and your permanents")
    void grantsHexproofToControllerAndPermanents() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast(false);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Promising the Gift also grants indestructible to your permanents")
    void giftGrantsIndestructibleToPermanents() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The granted hexproof expires at end of turn")
    void hexproofExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        cast(false);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The granted hexproof prevents an opponent from targeting you")
    void hexproofPreventsOpponentTargetingController() {
        addCreatureReady(player1, new GrizzlyBears());
        cast(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void promisedGiftDrawsOneCardForOpponent() {
        Plains gift = new Plains();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(gift, new Plains()));

        cast(true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(gift);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void decliningGiftDoesNotDrawForOpponent() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));

        cast(false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void protectionOnlyAppliesToControlledPermanentsPresentAtResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(true);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, existing, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    void giftedProtectionExpiresOnNextTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast(true);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void cast(boolean giftPromised) {
        harness.setHand(player1, List.of(new DawnsTruce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithGift(player1, 0, null, giftPromised);
        harness.passBothPriorities();
    }
}

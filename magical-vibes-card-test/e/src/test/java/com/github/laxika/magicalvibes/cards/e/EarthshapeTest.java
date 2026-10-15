package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Earthshape.class, Forest.class, GrizzlyBears.class, HillGiant.class, CanopyGorger.class})
class EarthshapeTest extends BaseCardTest {

    @Test
    void earthbendsLandAndProtectsCreaturesWithinItsPower() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());

        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertProtected(land);
        assertProtected(bears);
        assertProtected(giant);
        assertThat(gqs.hasKeyword(gd, largeCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, largeCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    void protectionWearsOffAtEndOfTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    @Test
    void cannotTargetLandControlledByOpponent() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void invalidatedLandTargetPreventsAllProtection() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        harness.assertInGraveyard(player1, "Earthshape");
    }

    @Test
    void repeatedEarthbendingUsesAccumulatedPowerForProtection() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        harness.setHand(player1, List.of(new Earthshape(), new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, land.getId());
        assertThat(gqs.hasKeyword(gd, largeCreature, Keyword.HEXPROOF)).isFalse();
        harness.castAndResolveInstant(player1, 0, land.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertProtected(largeCreature);
    }

    @Test
    void protectsOnlyCreaturesPresentUnderYourControlAtResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());
        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertProtected(bears);
        for (Permanent unprotected : List.of(opponentBears, otherLand, laterBears)) {
            assertThat(gqs.hasKeyword(gd, unprotected, Keyword.HEXPROOF)).isFalse();
            assertThat(gqs.hasKeyword(gd, unprotected, Keyword.INDESTRUCTIBLE)).isFalse();
        }
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    void sacrificedEarthbendedLandReturnsTappedAsANewUnprotectedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    void exiledEarthbendedLandReturnsTappedWithoutItsAnimation() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Earthshape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    private void assertProtected(Permanent permanent) {
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}

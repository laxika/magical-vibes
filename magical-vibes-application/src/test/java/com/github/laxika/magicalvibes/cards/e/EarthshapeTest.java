package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
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

    private void assertProtected(Permanent permanent) {
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}

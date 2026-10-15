package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MintharaOfTheAbsolute.class, GrizzlyBears.class})
class MintharaOfTheAbsoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures +1/+0 after a permanent you control leaves")
    void intensifiesAfterOwnPermanentLeaves() {
        harness.addToBattlefield(player1, new MintharaOfTheAbsolute());
        Permanent leavingBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent remainingBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, remainingBears)).isEqualTo(2);

        leaveBattlefield(leavingBears);
        harness.passBothPriorities();

        Permanent minthara = findPermanent(player1, "Minthara of the Absolute");
        assertThat(gd.getCardIntensity(minthara.getCard().getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, minthara)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, remainingBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Intensifies only once each turn")
    void intensifiesOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MintharaOfTheAbsolute());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        leaveBattlefield(firstBears);
        harness.passBothPriorities();
        leaveBattlefield(secondBears);
        harness.passBothPriorities();

        Permanent minthara = findPermanent(player1, "Minthara of the Absolute");
        assertThat(gd.getCardIntensity(minthara.getCard().getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's permanent")
    void ignoresOpponentsPermanentLeaving() {
        harness.addToBattlefield(player1, new MintharaOfTheAbsolute());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        leaveBattlefield(opponentBears);
        harness.passBothPriorities();

        Permanent minthara = findPermanent(player1, "Minthara of the Absolute");
        assertThat(gd.getCardIntensity(minthara.getCard().getId())).isZero();
    }

    @Test
    @DisplayName("Intensifies when Minthara itself leaves")
    void intensifiesWhenSelfLeaves() {
        Permanent minthara = harness.addToBattlefieldAndReturn(player1, new MintharaOfTheAbsolute());
        var cardId = minthara.getCard().getId();

        leaveBattlefield(minthara);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(cardId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Intensifies owned copies in other zones but not opponent-owned copies")
    void intensifiesOwnedCopiesAcrossZones() {
        harness.addToBattlefield(player1, new MintharaOfTheAbsolute());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MintharaOfTheAbsolute handCopy = new MintharaOfTheAbsolute();
        MintharaOfTheAbsolute graveyardCopy = new MintharaOfTheAbsolute();
        MintharaOfTheAbsolute exiledCopy = new MintharaOfTheAbsolute();
        MintharaOfTheAbsolute opponentCopy = new MintharaOfTheAbsolute();
        harness.setHand(player1, java.util.List.of(handCopy));
        harness.setGraveyard(player1, java.util.List.of(graveyardCopy));
        harness.setExile(player1, java.util.List.of(exiledCopy));
        harness.setHand(player2, java.util.List.of(opponentCopy));

        leaveBattlefield(bears);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(handCopy.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(graveyardCopy.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(exiledCopy.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponentCopy.getId())).isZero();
    }

    @Test
    @DisplayName("Leaving after an earlier trigger does not intensify again that turn")
    void selfLeavingSharesOncePerTurnLimit() {
        Permanent minthara = harness.addToBattlefieldAndReturn(player1, new MintharaOfTheAbsolute());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        leaveBattlefield(bears);
        harness.passBothPriorities();
        leaveBattlefield(minthara);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(minthara.getCard().getId())).isEqualTo(1);
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}

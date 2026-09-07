package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedSewerWitch.class, RuinRat.class})
class TwistedSewerWitchTest extends BaseCardTest {

    @Test
    void createsNonblockingRatAndAttachesWickedRolesToEachControlledRat() {
        Permanent opposingRat = harness.addToBattlefieldAndReturn(player2, new RuinRat());
        Permanent existingRat = harness.addToBattlefieldAndReturn(player1, new RuinRat());

        castAndResolve();

        Permanent createdRat = findPermanents(player1, "Rat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(bls.canBlock(gd, createdRat)).isFalse();

        List<Permanent> roles = findPermanents(player1, "Wicked");
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(existingRat.getId(), createdRat.getId());
        assertThat(gqs.getEffectivePower(gd, existingRat)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, createdRat)).isEqualTo(2);
        assertThat(findPermanents(player2, "Wicked")).isEmpty();
        assertThat(opposingRat.getAttachedTo()).isNull();
    }

    @Test
    void wickedRoleMakesEachOpponentLoseLifeWhenItDies() {
        castAndResolve();

        Permanent role = findPermanent(player1, "Wicked");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, role));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new TwistedSewerWitch()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

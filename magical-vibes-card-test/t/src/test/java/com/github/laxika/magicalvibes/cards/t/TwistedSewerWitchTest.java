package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuinRat;
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

    @Test
    void secondWitchReplacesOldRolesWithoutStackingTheirBoosts() {
        castAndResolve();
        Permanent firstRat = findPermanent(player1, "Rat");
        Permanent firstRole = findPermanent(player1, "Wicked");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        castAndResolve();
        harness.passBothPriorities();

        List<Permanent> roles = findPermanents(player1, "Wicked");
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Permanent::getId).doesNotContain(firstRole.getId());
        assertThat(roles.stream().filter(role -> firstRat.getId().equals(role.getAttachedTo())))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, firstRat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstRat)).isEqualTo(2);
        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    void ratDyingPutsItsRoleIntoGraveyardAndDrainsOpponent() {
        castAndResolve();
        Permanent rat = findPermanent(player1, "Rat");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rat));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    void doesNotAttachRolesToNonRatCreatures() {
        Permanent existingWitch = harness.addToBattlefieldAndReturn(player1, new TwistedSewerWitch());

        castAndResolve();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(findPermanent(player1, "Wicked").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Rat").getId())
                .isNotEqualTo(existingWitch.getId());
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new TwistedSewerWitch(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

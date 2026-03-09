package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.cards.c.ColossusOfSardia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeedbornMuse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UntapStepServiceTest extends BaseCardTest {

    private Permanent addAndGetPermanent(Player player, Card card) {
        harness.addToBattlefield(player, card);
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        return bf.get(bf.size() - 1);
    }

    @Nested
    @DisplayName("Normal untap behavior")
    class NormalUntap {

        @Test
        @DisplayName("Untaps tapped permanents for the active player")
        void untapsTappedPermanents() {
            harness.forceActivePlayer(player1);
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());
            perm.tap();

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(perm.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Does not untap the non-active player's permanents")
        void doesNotUntapNonActivePlayerPermanents() {
            harness.forceActivePlayer(player1);
            Permanent perm = addAndGetPermanent(player2, new GrizzlyBears());
            perm.tap();

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Clears summoning sickness")
        void clearsSummoningSickness() {
            harness.forceActivePlayer(player1);
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());
            perm.setSummoningSick(true);

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(perm.isSummoningSick()).isFalse();
        }

        @Test
        @DisplayName("Clears loyalty ability used flag")
        void clearsLoyaltyAbilityUsedFlag() {
            harness.forceActivePlayer(player1);
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());
            perm.setLoyaltyAbilityUsedThisTurn(true);

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(perm.isLoyaltyAbilityUsedThisTurn()).isFalse();
        }
    }

    @Nested
    @DisplayName("Doesn't untap effects")
    class DoesntUntap {

        @Test
        @DisplayName("Permanent with DoesntUntapDuringUntapStepEffect stays tapped")
        void doesntUntapWithStaticEffect() {
            harness.forceActivePlayer(player1);
            Permanent colossus = addAndGetPermanent(player1, new ColossusOfSardia());
            colossus.tap();

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(colossus.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with DoesntUntapDuringUntapStepEffect still clears summoning sickness")
        void clearsSummoningSicknessEvenWhenDoesntUntap() {
            harness.forceActivePlayer(player1);
            Permanent colossus = addAndGetPermanent(player1, new ColossusOfSardia());
            colossus.tap();
            colossus.setSummoningSick(true);

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(colossus.isSummoningSick()).isFalse();
        }
    }

    @Nested
    @DisplayName("Seedborn Muse untap")
    class SeedbornMuseUntap {

        @Test
        @DisplayName("Non-active player's permanents untap when they control Seedborn Muse")
        void untapsNonActivePlayerWithSeedbornMuse() {
            harness.forceActivePlayer(player1);
            harness.addToBattlefield(player2, new SeedbornMuse());
            Permanent tappedPerm = addAndGetPermanent(player2, new GrizzlyBears());
            tappedPerm.tap();

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(tappedPerm.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Active player's Seedborn Muse does not untap opponent's permanents during own untap step")
        void activePlayerSeedbornMuseDoesNotUntapOpponent() {
            harness.forceActivePlayer(player1);
            harness.addToBattlefield(player1, new SeedbornMuse());
            Permanent tappedPerm = addAndGetPermanent(player2, new GrizzlyBears());
            tappedPerm.tap();

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            // Player 2's perm stays tapped — player 1's Seedborn Muse only fires
            // during OTHER players' untap steps, not player 1's own
            assertThat(tappedPerm.isTapped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stale untap lock cleanup")
    class StaleUntapLockCleanup {

        @Test
        @DisplayName("Removes untap lock when source permanent is no longer on the battlefield")
        void removesLockWhenSourceGone() {
            harness.forceActivePlayer(player1);
            Permanent lockedPerm = addAndGetPermanent(player1, new GrizzlyBears());
            lockedPerm.tap();
            lockedPerm.getUntapPreventedByPermanentIds().add(UUID.randomUUID()); // non-existent source

            UntapStepService svc = new UntapStepService(gqs, harness.getGameBroadcastService());
            svc.untapPermanents(gd, player1.getId());

            assertThat(lockedPerm.getUntapPreventedByPermanentIds()).isEmpty();
            assertThat(lockedPerm.isTapped()).isFalse();
        }
    }
}

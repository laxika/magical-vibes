package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UntapStepServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @InjectMocks
    private UntapStepService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
    }

    private static Card createCardWithName(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    @Nested
    @DisplayName("Normal untap behavior")
    class NormalUntap {

        @Test
        @DisplayName("Untaps tapped permanents for the active player")
        void untapsTappedPermanents() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isFalse();
            verify(gameBroadcastService).logAndBroadcast(gd, "Player1 untaps their permanents.");
        }

        @Test
        @DisplayName("Does not untap the non-active player's permanents")
        void doesNotUntapNonActivePlayerPermanents() {
            Permanent perm = addPermanent(player2Id, createCardWithName("Grizzly Bears"));
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Clears summoning sickness")
        void clearsSummoningSickness() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.setSummoningSick(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isSummoningSick()).isFalse();
        }

        @Test
        @DisplayName("Clears loyalty ability used flag")
        void clearsLoyaltyAbilityUsedFlag() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.setLoyaltyAbilityUsedThisTurn(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isLoyaltyAbilityUsedThisTurn()).isFalse();
        }
    }

    @Nested
    @DisplayName("Doesn't untap effects")
    class DoesntUntap {

        @Test
        @DisplayName("Permanent with DoesntUntapDuringUntapStepEffect stays tapped")
        void doesntUntapWithStaticEffect() {
            Card card = createCardWithName("Colossus of Sardia");
            card.addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with AttachedCreatureDoesntUntapEffect stays tapped")
        void doesntUntapWithAttachedAuraEffect() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.tap();

            when(gameQueryService.hasAuraWithEffect(gd, perm, AttachedCreatureDoesntUntapEffect.class))
                    .thenReturn(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with active untap lock stays tapped")
        void staysTappedWithActiveUntapLock() {
            Permanent lockedPerm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            lockedPerm.tap();
            UUID sourceId = UUID.randomUUID();
            lockedPerm.getUntapPreventedByPermanentIds().add(sourceId);

            // Source permanent exists and is still tapped — lock is valid
            Permanent sourcePerm = new Permanent(createCardWithName("Icy Manipulator"));
            sourcePerm.tap();
            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(sourcePerm);

            sut.untapPermanents(gd, player1Id);

            assertThat(lockedPerm.isTapped()).isTrue();
            assertThat(lockedPerm.getUntapPreventedByPermanentIds()).containsExactly(sourceId);
        }

        @Test
        @DisplayName("Permanent with skipUntapCount stays tapped and decrements counter")
        void skipsUntapAndDecrementsCounter() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.tap();
            perm.setSkipUntapCount(2);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
            assertThat(perm.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Permanent with DoesntUntapDuringUntapStepEffect still clears summoning sickness")
        void clearsSummoningSicknessEvenWhenDoesntUntap() {
            Card card = createCardWithName("Colossus of Sardia");
            card.addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();
            perm.setSummoningSick(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isSummoningSick()).isFalse();
        }
    }

    @Nested
    @DisplayName("May not untap effects")
    class MayNotUntap {

        @Test
        @DisplayName("Tapped permanent with MayNotUntapDuringUntapStepEffect queues a PendingMayAbility")
        void queuesPendingMayAbility() {
            Card card = createCardWithName("Verity Circle Target");
            card.addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            // Permanent stays tapped — choice is deferred
            assertThat(perm.isTapped()).isTrue();
            // A PendingMayAbility was queued for the controller
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Untap");
        }
    }

    @Nested
    @DisplayName("Seedborn Muse untap")
    class SeedbornMuseUntap {

        @Test
        @DisplayName("Non-active player's permanents untap when they control Seedborn Muse")
        void untapsNonActivePlayerWithSeedbornMuse() {
            // Player 2 controls Seedborn Muse
            Card museCard = createCardWithName("Seedborn Muse");
            museCard.addEffect(EffectSlot.STATIC,
                    new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP));
            addPermanent(player2Id, museCard);

            // Player 2 also has a tapped creature
            Permanent tappedPerm = addPermanent(player2Id, createCardWithName("Grizzly Bears"));
            tappedPerm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(tappedPerm.isTapped()).isFalse();
            verify(gameBroadcastService).logAndBroadcast(gd,
                    "Player2 untaps their permanents due to Seedborn Muse.");
        }

        @Test
        @DisplayName("Filtered untap effect only untaps matching permanents")
        void filteredEffectOnlyUntapsMatchingPermanents() {
            // Player 2 controls a permanent with a filtered untap effect
            PermanentPredicate filter = new PermanentPredicate() {};
            Card effectCard = createCardWithName("Filtered Untapper");
            effectCard.addEffect(EffectSlot.STATIC,
                    new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP, filter));
            addPermanent(player2Id, effectCard);

            // Player 2 has two tapped creatures — only one matches the filter
            Permanent matchingPerm = addPermanent(player2Id, createCardWithName("Matching Creature"));
            matchingPerm.tap();
            Permanent nonMatchingPerm = addPermanent(player2Id, createCardWithName("Non-Matching Creature"));
            nonMatchingPerm.tap();

            // Default: permanents don't match the filter
            when(gameQueryService.matchesPermanentPredicate(eq(gd), any(), eq(filter))).thenReturn(false);
            // Only the matching permanent passes the filter
            when(gameQueryService.matchesPermanentPredicate(gd, matchingPerm, filter)).thenReturn(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(matchingPerm.isTapped()).isFalse();
            assertThat(nonMatchingPerm.isTapped()).isTrue();
            verify(gameBroadcastService).logAndBroadcast(gd,
                    "Player2 untaps some permanents during opponent's untap step.");
        }

        @Test
        @DisplayName("Active player's Seedborn Muse does not untap opponent's permanents during own untap step")
        void activePlayerSeedbornMuseDoesNotUntapOpponent() {
            // Player 1 (active) controls Seedborn Muse
            Card museCard = createCardWithName("Seedborn Muse");
            museCard.addEffect(EffectSlot.STATIC,
                    new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP));
            addPermanent(player1Id, museCard);

            // Player 2 has a tapped creature
            Permanent tappedPerm = addPermanent(player2Id, createCardWithName("Grizzly Bears"));
            tappedPerm.tap();

            sut.untapPermanents(gd, player1Id);

            // Player 2's perm stays tapped — player 1's Seedborn Muse only fires
            // during OTHER players' untap steps, not player 1's own
            assertThat(tappedPerm.isTapped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stale untap lock cleanup")
    class StaleUntapLockCleanup {

        @Test
        @DisplayName("Removes untap lock when source permanent exists but is no longer tapped")
        void removesLockWhenSourceUntapped() {
            Permanent lockedPerm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            lockedPerm.tap();
            UUID sourceId = UUID.randomUUID();
            lockedPerm.getUntapPreventedByPermanentIds().add(sourceId);

            // Source permanent exists but is untapped — lock is stale
            Permanent sourcePerm = new Permanent(createCardWithName("Icy Manipulator"));
            // sourcePerm is untapped by default
            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(sourcePerm);

            sut.untapPermanents(gd, player1Id);

            assertThat(lockedPerm.getUntapPreventedByPermanentIds()).isEmpty();
            assertThat(lockedPerm.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Removes untap lock when source permanent is no longer on the battlefield")
        void removesLockWhenSourceGone() {
            Permanent lockedPerm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            lockedPerm.tap();
            UUID staleSourceId = UUID.randomUUID();
            lockedPerm.getUntapPreventedByPermanentIds().add(staleSourceId);

            // Source permanent no longer exists on the battlefield
            when(gameQueryService.findPermanentById(gd, staleSourceId)).thenReturn(null);

            sut.untapPermanents(gd, player1Id);

            assertThat(lockedPerm.getUntapPreventedByPermanentIds()).isEmpty();
            assertThat(lockedPerm.isTapped()).isFalse();
        }
    }
}

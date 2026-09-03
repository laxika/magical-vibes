package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.GameLog;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.StorageMatrixEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.UntapPreventionSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@ExtendWith(MockitoExtension.class)
class UntapStepServiceTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private GameLogService gameLogService;
    @Mock
    private PhasingService phasingService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private DayNightService dayNightService;
    @Spy
    private UntapPreventionSupport untapPreventionSupport =
            new UntapPreventionSupport(org.mockito.Mockito.mock(ConditionEvaluationService.class));

    // Real support so untapPermanent actually untaps; its trigger service is an inert mock.
    @Spy
    private TapUntapSupport tapUntapSupport =
            new TapUntapSupport(org.mockito.Mockito.mock(TriggerCollectionService.class),
                    org.mockito.Mockito.mock(CreatureControlService.class),
                    org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.battlefield.UntapLockReleaseService.class));

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
            verify(gameLogService).append(gd, GameLog.text("Player1 untaps their permanents."));
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
            perm.setLoyaltyActivationsThisTurn(1);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.getLoyaltyActivationsThisTurn()).isZero();
        }
    }

    @Nested
    @DisplayName("Doesn't untap effects")
    class DoesntUntap {

        @Test
        @DisplayName("Permanent with self-scoped DoesntUntapEffect stays tapped")
        void doesntUntapWithStaticEffect() {
            Card card = createCardWithName("Colossus of Sardia");
            card.addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with DoesntUntapWithCounterEffect stays tapped while it has such a counter")
        void doesntUntapWhileCounterPresent() {
            Card card = createCardWithName("Land Cap");
            card.addEffect(EffectSlot.STATIC, new DoesntUntapWithCounterEffect(CounterType.DEPLETION));
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();
            perm.setCounterCount(CounterType.DEPLETION, 1);

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with DoesntUntapWithCounterEffect untaps once the counter is gone")
        void untapsWhenCounterAbsent() {
            Card card = createCardWithName("Land Cap");
            card.addEffect(EffectSlot.STATIC, new DoesntUntapWithCounterEffect(CounterType.DEPLETION));
            Permanent perm = addPermanent(player1Id, card);
            perm.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(perm.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Attached Aura can lock its host based on a counter on the Aura")
        void attachedAuraChecksItsOwnCounter() {
            Permanent creature = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            creature.tap();
            Card auraCard = createCardWithName("Cocoon");
            auraCard.addEffect(EffectSlot.STATIC,
                    DoesntUntapWithCounterEffect.enchanted(CounterType.PUPA));
            Permanent aura = addPermanent(player1Id, auraCard);
            aura.setAttachedTo(creature.getId());
            aura.setCounterCount(CounterType.PUPA, 1);

            sut.untapPermanents(gd, player1Id);

            assertThat(creature.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Attached Aura can lock its host based on a counter on the host")
        void attachedAuraChecksEnchantedPermanentCounter() {
            Permanent creature = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            creature.tap();
            creature.setCounterCount(CounterType.SLEEP, 1);
            Card auraCard = createCardWithName("Venarian Gold");
            auraCard.addEffect(EffectSlot.STATIC,
                    DoesntUntapWithCounterEffect.enchantedWithCounterOnEnchantedPermanent(CounterType.SLEEP));
            Permanent aura = addPermanent(player1Id, auraCard);
            aura.setAttachedTo(creature.getId());

            sut.untapPermanents(gd, player1Id);

            assertThat(creature.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Permanent with enchanted-scope DoesntUntapEffect stays tapped")
        void doesntUntapWithAttachedAuraEffect() {
            Permanent perm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            perm.tap();

            when(gameQueryService.hasAuraWithEffect(gd, perm, DoesntUntapEffect.class))
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
        @DisplayName("Permanent with active on-battlefield untap lock stays tapped")
        void staysTappedWithActiveOnBattlefieldLock() {
            Permanent lockedPerm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            lockedPerm.tap();
            UUID sourceId = UUID.randomUUID();
            lockedPerm.getUntapPreventedWhileSourceOnBattlefieldIds().add(sourceId);

            // Source permanent exists on the battlefield (not tapped — doesn't matter)
            Permanent sourcePerm = new Permanent(createCardWithName("Time of Ice"));
            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(sourcePerm);

            sut.untapPermanents(gd, player1Id);

            assertThat(lockedPerm.isTapped()).isTrue();
            assertThat(lockedPerm.getUntapPreventedWhileSourceOnBattlefieldIds()).containsExactly(sourceId);
        }

        @Test
        @DisplayName("On-battlefield untap lock is removed when source leaves battlefield")
        void onBattlefieldLockRemovedWhenSourceGone() {
            Permanent lockedPerm = addPermanent(player1Id, createCardWithName("Grizzly Bears"));
            lockedPerm.tap();
            UUID sourceId = UUID.randomUUID();
            lockedPerm.getUntapPreventedWhileSourceOnBattlefieldIds().add(sourceId);

            // Source permanent is gone
            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            sut.untapPermanents(gd, player1Id);

            assertThat(lockedPerm.isTapped()).isFalse();
            assertThat(lockedPerm.getUntapPreventedWhileSourceOnBattlefieldIds()).isEmpty();
        }

        @Test
        @DisplayName("Permanent with self-scoped DoesntUntapEffect still clears summoning sickness")
        void clearsSummoningSicknessEvenWhenDoesntUntap() {
            Card card = createCardWithName("Colossus of Sardia");
            card.addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
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
        @DisplayName("Self-scoped effect untaps only its source during an opponent's untap step")
        void selfScopedEffectOnlyUntapsSource() {
            Card waterskinCard = createCardWithName("Bender's Waterskin");
            waterskinCard.addEffect(EffectSlot.STATIC,
                    new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                            TurnStep.UNTAP, null, TapUntapScope.SELF));
            Permanent waterskin = addPermanent(player2Id, waterskinCard);
            waterskin.tap();
            Permanent otherPermanent = addPermanent(player2Id, createCardWithName("Other Permanent"));
            otherPermanent.tap();

            sut.untapPermanents(gd, player1Id);

            assertThat(waterskin.isTapped()).isFalse();
            assertThat(otherPermanent.isTapped()).isTrue();
        }

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
            verify(gameLogService).append(gd, GameLog.text("Player2 untaps their permanents due to Seedborn Muse."));
        }

        @Test
        @DisplayName("Filtered untap effect only untaps matching permanents")
        void filteredEffectOnlyUntapsMatchingPermanents() {
            // Player 2 controls a permanent with a filtered untap effect
            PermanentPredicate filter = new PermanentTruePredicate();
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
            when(predicateEvaluationService.matchesPermanentPredicate(any(Permanent.class), eq(filter),
                    any(FilterContext.class))).thenReturn(false);
            // Only the matching permanent passes the filter
            when(predicateEvaluationService.matchesPermanentPredicate(eq(matchingPerm), eq(filter),
                    any(FilterContext.class))).thenReturn(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(matchingPerm.isTapped()).isFalse();
            assertThat(nonMatchingPerm.isTapped()).isTrue();
            verify(gameLogService).append(gd, GameLog.text("Player2 untaps some permanents during opponent's untap step."));
        }

        @Test
        @DisplayName("Source-relative filter only untaps the effect's source")
        void sourceRelativeFilterOnlyUntapsSource() {
            PermanentPredicate filter = new PermanentIsSourcePermanentPredicate();
            Card effectCard = createCardWithName("Source Untapper");
            effectCard.addEffect(EffectSlot.STATIC,
                    new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP, filter));
            Permanent source = addPermanent(player2Id, effectCard);
            source.tap();

            Permanent otherPermanent = addPermanent(player2Id, createCardWithName("Other Permanent"));
            otherPermanent.tap();

            when(predicateEvaluationService.matchesPermanentPredicate(eq(source), eq(filter), any()))
                    .thenReturn(true);

            sut.untapPermanents(gd, player1Id);

            assertThat(source.isTapped()).isFalse();
            assertThat(otherPermanent.isTapped()).isTrue();
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
    @DisplayName("Storage Matrix restriction")
    class StorageMatrixRestriction {

        @Test
        @DisplayName("Applies when an untapped Storage Matrix is out and the active player has a tapped permanent")
        void appliesWithUntappedMatrixAndTappedPermanent() {
            Card matrixCard = createCardWithName("Storage Matrix");
            matrixCard.addEffect(EffectSlot.STATIC, new StorageMatrixEffect());
            addPermanent(player1Id, matrixCard);
            addPermanent(player1Id, createCardWithName("Grizzly Bears")).tap();

            assertThat(sut.storageMatrixRestrictionApplies(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Does not apply when the Storage Matrix is tapped")
        void doesNotApplyWhenMatrixTapped() {
            Card matrixCard = createCardWithName("Storage Matrix");
            matrixCard.addEffect(EffectSlot.STATIC, new StorageMatrixEffect());
            addPermanent(player1Id, matrixCard).tap();
            addPermanent(player1Id, createCardWithName("Grizzly Bears")).tap();

            assertThat(sut.storageMatrixRestrictionApplies(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Does not apply when the active player has nothing tapped")
        void doesNotApplyWithNothingTapped() {
            Card matrixCard = createCardWithName("Storage Matrix");
            matrixCard.addEffect(EffectSlot.STATIC, new StorageMatrixEffect());
            addPermanent(player1Id, matrixCard);

            assertThat(sut.storageMatrixRestrictionApplies(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Restricted untap only untaps permanents matching the chosen-type predicate")
        void restrictedUntapOnlyUntapsMatching() {
            PermanentPredicate chosenType = new PermanentTruePredicate();
            Permanent matching = addPermanent(player1Id, createCardWithName("Matching"));
            matching.tap();
            Permanent nonMatching = addPermanent(player1Id, createCardWithName("Non-Matching"));
            nonMatching.tap();

            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), any(), eq(chosenType))).thenReturn(false);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, matching, chosenType)).thenReturn(true);

            sut.untapPermanents(gd, player1Id, chosenType);

            assertThat(matching.isTapped()).isFalse();
            assertThat(nonMatching.isTapped()).isTrue();
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

package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StateTriggerKey;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StateTriggerServiceTest {

    @Mock
    private GameBroadcastService gameBroadcastService;

    @InjectMocks
    private StateTriggerService sut;

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

    private static Card createCardWithStateTrigger(String name, StateTriggerEffect trigger) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.addEffect(EffectSlot.STATE_TRIGGERED, trigger);
        return card;
    }

    @Nested
    @DisplayName("checkStateTriggers")
    class CheckStateTriggers {

        @Test
        @DisplayName("Fires trigger when predicate is satisfied and not already on stack")
        void firesWhenPredicateSatisfied() {
            List<CardEffect> effects = List.of(new GainLifeEffect(3));
            StateTriggerEffect trigger = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    effects,
                    "Test trigger"
            );
            Card card = createCardWithStateTrigger("Trigger Card", trigger);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getDescription()).isEqualTo("Test trigger");
            assertThat(entry.getEffectsToResolve()).isEqualTo(effects);
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getStateTriggerEffectIndex()).isZero();
            assertThat(gd.stateTriggerOnStack).contains(new StateTriggerKey(perm.getId(), 0));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Test trigger triggers."));
        }

        @Test
        @DisplayName("Does not fire when predicate is not satisfied")
        void doesNotFireWhenPredicateNotSatisfied() {
            StateTriggerEffect trigger = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> false,
                    List.of(new GainLifeEffect(3)),
                    "Never trigger"
            );
            Card card = createCardWithStateTrigger("No Trigger Card", trigger);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.stateTriggerOnStack).doesNotContain(new StateTriggerKey(perm.getId(), 0));
            verifyNoInteractions(gameBroadcastService);
        }

        @Test
        @DisplayName("Does not retrigger while already on stack — CR 603.8")
        void doesNotRetriggerWhileOnStack() {
            StateTriggerEffect trigger = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(3)),
                    "Once trigger"
            );
            Card card = createCardWithStateTrigger("Already On Stack", trigger);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.stateTriggerOnStack.add(new StateTriggerKey(perm.getId(), 0));

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(gameBroadcastService);
        }

        @Test
        @DisplayName("Checks both players in APNAP order")
        void checksPlayersInApnapOrder() {
            List<CardEffect> effects1 = List.of(new DealDamageToAnyTargetEffect(1));
            StateTriggerEffect trigger1 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    effects1,
                    "Player1 trigger"
            );
            Card card1 = createCardWithStateTrigger("P1 Card", trigger1);
            Permanent perm1 = new Permanent(card1);
            gd.playerBattlefields.get(player1Id).add(perm1);

            List<CardEffect> effects2 = List.of(new GainLifeEffect(2));
            StateTriggerEffect trigger2 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    effects2,
                    "Player2 trigger"
            );
            Card card2 = createCardWithStateTrigger("P2 Card", trigger2);
            Permanent perm2 = new Permanent(card2);
            gd.playerBattlefields.get(player2Id).add(perm2);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.get(0).getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.get(1).getControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("Handles multiple permanents with triggers for same player")
        void handlesMultiplePermanentsForSamePlayer() {
            StateTriggerEffect trigger1 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(1)),
                    "Trigger A"
            );
            StateTriggerEffect trigger2 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(2)),
                    "Trigger B"
            );
            Card card1 = createCardWithStateTrigger("Card A", trigger1);
            Card card2 = createCardWithStateTrigger("Card B", trigger2);
            Permanent perm1 = new Permanent(card1);
            Permanent perm2 = new Permanent(card2);
            gd.playerBattlefields.get(player1Id).add(perm1);
            gd.playerBattlefields.get(player1Id).add(perm2);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stateTriggerOnStack).containsExactlyInAnyOrder(
                    new StateTriggerKey(perm1.getId(), 0),
                    new StateTriggerKey(perm2.getId(), 0));
        }

        @Test
        @DisplayName("Skips permanents without STATE_TRIGGERED effects")
        void skipsPermanentsWithoutStateTriggers() {
            Card card = new Card();
            card.setName("Vanilla Creature");
            card.setType(CardType.CREATURE);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.stateTriggerOnStack).isEmpty();
            verifyNoInteractions(gameBroadcastService);
        }

        @Test
        @DisplayName("Rejects non-StateTriggerEffect registered in STATE_TRIGGERED slot")
        void rejectsNonStateTriggerEffectInSlot() {
            Card card = new Card();
            card.setName("Odd Card");
            card.setType(CardType.ENCHANTMENT);

            assertThatThrownBy(() -> card.addEffect(EffectSlot.STATE_TRIGGERED, new GainLifeEffect(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("STATE_TRIGGERED slot requires StateTriggerEffect");
        }

        @Test
        @DisplayName("Fires all state triggers on same permanent independently")
        void firesAllTriggersOnSamePermanent() {
            StateTriggerEffect trigger1 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(1)),
                    "First trigger"
            );
            StateTriggerEffect trigger2 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(2)),
                    "Second trigger"
            );
            Card card = new Card();
            card.setName("Multi Trigger Card");
            card.setType(CardType.ENCHANTMENT);
            card.addEffect(EffectSlot.STATE_TRIGGERED, trigger1);
            card.addEffect(EffectSlot.STATE_TRIGGERED, trigger2);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.get(0).getDescription()).isEqualTo("First trigger");
            assertThat(gd.stack.get(0).getStateTriggerEffectIndex()).isZero();
            assertThat(gd.stack.get(1).getDescription()).isEqualTo("Second trigger");
            assertThat(gd.stack.get(1).getStateTriggerEffectIndex()).isEqualTo(1);
            assertThat(gd.stateTriggerOnStack).containsExactlyInAnyOrder(
                    new StateTriggerKey(perm.getId(), 0),
                    new StateTriggerKey(perm.getId(), 1));
        }

        @Test
        @DisplayName("Does not retrigger specific effect while on stack — other effects still fire")
        void doesNotRetriggerSpecificEffectWhileOnStack() {
            StateTriggerEffect trigger1 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(1)),
                    "First trigger"
            );
            StateTriggerEffect trigger2 = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    List.of(new GainLifeEffect(2)),
                    "Second trigger"
            );
            Card card = new Card();
            card.setName("Multi Trigger Card");
            card.setType(CardType.ENCHANTMENT);
            card.addEffect(EffectSlot.STATE_TRIGGERED, trigger1);
            card.addEffect(EffectSlot.STATE_TRIGGERED, trigger2);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            // Mark only the first trigger as already on stack
            gd.stateTriggerOnStack.add(new StateTriggerKey(perm.getId(), 0));

            sut.checkStateTriggers(gd);

            // Only the second trigger fires
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).isEqualTo("Second trigger");
        }

        @Test
        @DisplayName("Handles null battlefield gracefully")
        void handlesNullBattlefield() {
            gd.playerBattlefields.remove(player2Id);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Predicate receives correct gameData, permanent, and controllerId")
        void predicateReceivesCorrectArguments() {
            UUID[] capturedControllerId = new UUID[1];
            Permanent[] capturedPerm = new Permanent[1];
            GameData[] capturedGd = new GameData[1];

            StateTriggerEffect trigger = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> {
                        capturedGd[0] = gameData;
                        capturedPerm[0] = perm;
                        capturedControllerId[0] = controllerId;
                        return false;
                    },
                    List.of(new GainLifeEffect(1)),
                    "Arg check"
            );
            Card card = createCardWithStateTrigger("Arg Card", trigger);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player2Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(capturedGd[0]).isSameAs(gd);
            assertThat(capturedPerm[0]).isSameAs(perm);
            assertThat(capturedControllerId[0]).isEqualTo(player2Id);
        }
    }

    @Nested
    @DisplayName("cleanupResolvedStateTrigger")
    class CleanupResolvedStateTrigger {

        @Test
        @DisplayName("Removes state trigger key from stateTriggerOnStack for triggered ability")
        void removesTrackingForTriggeredAbility() {
            UUID sourcePermanentId = UUID.randomUUID();
            gd.stateTriggerOnStack.add(new StateTriggerKey(sourcePermanentId, 0));

            Card card = new Card();
            card.setName("Trigger Card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    player1Id,
                    "Test trigger",
                    List.of(),
                    null,
                    sourcePermanentId
            );
            entry.setStateTriggerEffectIndex(0);

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).doesNotContain(new StateTriggerKey(sourcePermanentId, 0));
        }

        @Test
        @DisplayName("Does not remove tracking when sourcePermanentId is null")
        void doesNotRemoveWhenSourcePermanentIdIsNull() {
            UUID otherId = UUID.randomUUID();
            StateTriggerKey otherKey = new StateTriggerKey(otherId, 0);
            gd.stateTriggerOnStack.add(otherKey);

            Card card = new Card();
            card.setName("No Source Card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    player1Id,
                    "No source trigger",
                    List.of()
            );

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(otherKey);
        }

        @Test
        @DisplayName("Does not remove tracking for non-triggered-ability entry types")
        void doesNotRemoveForNonTriggeredAbility() {
            UUID sourcePermanentId = UUID.randomUUID();
            StateTriggerKey key = new StateTriggerKey(sourcePermanentId, 0);
            gd.stateTriggerOnStack.add(key);

            Card card = new Card();
            card.setName("Creature Card");
            StackEntry entry = new StackEntry(card, player1Id);

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(key);
        }

        @Test
        @DisplayName("Does not remove tracking for triggered ability without stateTriggerEffectIndex")
        void doesNotRemoveForNonStateTrigger() {
            UUID sourcePermanentId = UUID.randomUUID();
            StateTriggerKey key = new StateTriggerKey(sourcePermanentId, 0);
            gd.stateTriggerOnStack.add(key);

            Card card = new Card();
            card.setName("ETB Card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    player1Id,
                    "ETB trigger",
                    List.of(),
                    null,
                    sourcePermanentId
            );
            // stateTriggerEffectIndex defaults to -1 (not a state trigger)

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(key);
        }

        @Test
        @DisplayName("Allows trigger to fire again after cleanup")
        void allowsRetriggerAfterCleanup() {
            List<CardEffect> effects = List.of(new GainLifeEffect(5));
            StateTriggerEffect trigger = new StateTriggerEffect(
                    (gameData, perm, controllerId) -> true,
                    effects,
                    "Repeating trigger"
            );
            Card card = createCardWithStateTrigger("Repeater", trigger);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            // First fire
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();

            // While on stack, should not retrigger
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);

            // Cleanup (simulating resolution)
            sut.cleanupResolvedStateTrigger(gd, entry);
            gd.stack.clear();

            // Should fire again
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);
        }
    }
}

package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscardTriggerCollectorServiceTest {

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @InjectMocks
    private DiscardTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    // ===== ON_OPPONENT_DISCARDS — DealDamageToDiscardingPlayerEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — DealDamageToDiscardingPlayerEffect")
    class DiscardDamage {

        @Test
        @DisplayName("deals damage to the discarding player and returns true")
        void dealsDamageToDiscardingPlayer() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 2);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented globally")
        void noDamageWhenSourcePrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented for player")
        void noDamageWhenSourcePreventedForPlayer() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.isSourceDamagePreventedForPlayer(gd, player2Id, megrim.getId()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when color damage prevention applies")
        void noDamageWhenColorPreventionApplies() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player2Id), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when permanent is prevented from dealing damage")
        void noDamageWhenPermanentPrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            gd.permanentsPreventedFromDealingDamage.add(megrim.getId());
            int lifeBefore = gd.getLife(player2Id);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("tracks player as dealt damage this turn")
        void tracksPlayerDealtDamageThisTurn() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.playersDealtDamageThisTurn).contains(player2Id);
        }

        @Test
        @DisplayName("gives poison counters when damage should be dealt as infect")
        void givesPoisonCountersWithInfect() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, player2Id)).thenReturn(true);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.playerPoisonCounters.getOrDefault(player2Id, 0)).isEqualTo(2);
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not give poison counters when player can't get them")
        void noPoisonWhenPlayerCantGetThem() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);
            // canPlayerGetPoisonCounters defaults to false

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.playerPoisonCounters).doesNotContainKey(player2Id);
        }

        @Test
        @DisplayName("does not track damage when prevention shield reduces damage to zero")
        void noDamageTrackingWhenShieldReducesToZero() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(0);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(0), any()))
                    .thenReturn(0);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
            assertThat(gd.playersDealtDamageThisTurn).doesNotContain(player2Id);
        }

        @Test
        @DisplayName("does not change life when player life can't change")
        void noLifeChangeWhenPrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            // canPlayerLifeChange defaults to false — life can't change

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }
    }

    // ===== ON_OPPONENT_DISCARDS — LoseLifeEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — LoseLifeEffect")
    class DiscardLifeLoss {

        @Test
        @DisplayName("causes the discarding player to lose life and returns true")
        void causesLifeLossOnDiscard() {
            Permanent enchantment = createPermanent("Liliana's Caress");
            var effect = new LoseLifeEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 2);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("does not change life when player life can't change")
        void noLifeLossWhenPrevented() {
            Permanent enchantment = createPermanent("Liliana's Caress");
            var effect = new LoseLifeEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            // canPlayerLifeChange defaults to false — life can't change

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }
    }

    // ===== ON_OPPONENT_DISCARDS — MayEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — MayEffect")
    class DiscardMay {

        @Test
        @DisplayName("queues may ability and returns true")
        void queuesMayAbility() {
            Permanent enchantment = createPermanent("Waste Not");
            var inner = new LoseLifeEffect(1);
            var effect = new MayEffect(inner, "Do you want to trigger?");
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }
}

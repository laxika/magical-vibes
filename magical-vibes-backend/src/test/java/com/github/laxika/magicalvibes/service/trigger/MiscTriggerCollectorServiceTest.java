package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
class MiscTriggerCollectorServiceTest {

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private ExileService exileService;

    @Mock
    private PermanentControlResolutionService permanentControlResolutionService;

    @InjectMocks
    private MiscTriggerCollectorService sut;

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

    // ===== ON_ALLY_PERMANENT_SACRIFICED — MayPayManaEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — MayPayManaEffect")
    class SacrificeMayPay {

        @Test
        @DisplayName("queues may-pay ability on stack when ally permanent is sacrificed")
        void queuesMayPayAbilityOnSacrifice() {
            Permanent perm = createPermanent("Furnace Celebration");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayPayManaEffect("{2}", inner, "Pay {2}?");
            var ctx = new TriggerContext.AllySacrificed(player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Furnace Celebration");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayPayManaEffect("{2}", inner, "Pay {2}?");
            var ctx = new TriggerContext.AllySacrificed(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — MayEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — MayEffect")
    class SacrificeMay {

        @Test
        @DisplayName("queues may ability on stack when ally permanent is sacrificed")
        void queuesMayAbilityOnSacrifice() {
            Permanent perm = createPermanent("Some Card");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayEffect(inner, "Do you want to?");
            var ctx = new TriggerContext.AllySacrificed(player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Some Card");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayEffect(inner, "Do you want to?");
            var ctx = new TriggerContext.AllySacrificed(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — default CardEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — default CardEffect")
    class SacrificeDefault {

        @Test
        @DisplayName("puts triggered ability on stack for non-may effect")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.AllySacrificed(player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getDescription()).contains("Blood Artist");
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("stack entry includes the effect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(2, 2);
            var ctx = new TriggerContext.AllySacrificed(player1Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.AllySacrificed(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — GiveEnchantedPermanentControllerPoisonCountersEffect =====

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — GiveEnchantedPermanentControllerPoisonCountersEffect")
    class EnchantedPermanentTapPoison {

        @Test
        @DisplayName("puts triggered ability on stack with resolved effect containing tapped permanent's controller")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GiveEnchantedPermanentControllerPoisonCountersEffect(1);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Relic Putrescence");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
        }

        @Test
        @DisplayName("resolved effect bakes in the tapped permanent controller's ID")
        void resolvedEffectContainsControllerId() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GiveEnchantedPermanentControllerPoisonCountersEffect(1);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            var resolved = (GiveEnchantedPermanentControllerPoisonCountersEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.affectedPlayerId()).isEqualTo(player2Id);
            assertThat(resolved.amount()).isEqualTo(1);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GiveEnchantedPermanentControllerPoisonCountersEffect(1);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }

    // ===== ON_OPPONENT_LOSES_LIFE — MillOpponentOnLifeLossEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_LOSES_LIFE — MillOpponentOnLifeLossEffect")
    class LifeLossMill {

        @Test
        @DisplayName("mills opponent for the amount of life lost and returns true")
        void millsOpponentForLifeLost() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 3);

            gd.playerIdToName.put(player2Id, "Player2");

            boolean result = registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            assertThat(result).isTrue();
            verify(graveyardService).resolveMillPlayer(gd, player2Id, 3);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("mills correct amount for 1 life lost (singular log message)")
        void millsSingularAmount() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 1);

            gd.playerIdToName.put(player2Id, "Player2");

            registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 1);
        }

        @Test
        @DisplayName("broadcasts log message with player name and card count")
        void broadcastsLogMessage() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 5);

            gd.playerIdToName.put(player2Id, "Opponent");

            registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Mindcrank triggers — Opponent mills 5 cards."));
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — PutCountersOnSourceEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — PutCountersOnSourceEffect")
    class LifeGainPutCounters {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Ajani's Pridemate");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes the PutCountersOnSourceEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — TargetPlayerLosesLifeEqualToLifeGainedEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — TargetPlayerLosesLifeEqualToLifeGainedEffect")
    class LifeGainOpponentLosesLife {

        @Test
        @DisplayName("puts triggered ability on stack targeting opponent with life loss equal to life gained")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new TargetPlayerLosesLifeEqualToLifeGainedEffect();
            var ctx = new TriggerContext.LifeGain(player1Id, 4);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Sanguine Bond");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("resolved effect has TargetPlayerLosesLifeEffect with correct amount")
        void resolvedEffectHasCorrectAmount() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new TargetPlayerLosesLifeEqualToLifeGainedEffect();
            var ctx = new TriggerContext.LifeGain(player1Id, 7);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            var resolved = (TargetPlayerLosesLifeEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.amount()).isEqualTo(7);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new TargetPlayerLosesLifeEqualToLifeGainedEffect();
            var ctx = new TriggerContext.LifeGain(player1Id, 4);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }

    // ===== ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — BoostSelfEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — BoostSelfEffect")
    class NoncombatDamageBoostSelf {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Chandra's Spitfire");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes the BoostSelfEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }
}

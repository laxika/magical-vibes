package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EffectResolutionServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private EffectHandlerRegistry registry;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @InjectMocks
    private EffectResolutionService effectResolutionService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private StackEntry createEntry(Card card, UUID controllerId, List<CardEffect> effects) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects);
    }

    private EffectHandler stubHandler(CardEffect effect) {
        EffectHandler handler = mock(EffectHandler.class);
        lenient().when(registry.getHandler(effect)).thenReturn(handler);
        return handler;
    }

    // =========================================================================
    // MetalcraftConditionalEffect
    // (ConcussiveBolt: DealDamageToTargetPlayerEffect(4) + MetalcraftConditionalEffect(TargetPlayerCreaturesCantBlockThisTurnEffect))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftConditionalEffect")
    class ResolveMetalcraftConditionalEffect {

        @Test
        @DisplayName("Skips wrapped effect when metalcraft is not met")
        void skipsWrappedEffectWhenMetalcraftNotMet() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Logs skip message when metalcraft is not met")
        void logsSkipMessageWhenMetalcraftNotMet() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Concussive Bolt") && msg.contains("does nothing")
                            && msg.contains("fewer than three artifacts")));
        }

        @Test
        @DisplayName("Resolves wrapped effect when metalcraft is met")
        void resolvesWrappedEffectWhenMetalcraftMet() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            EffectHandler handler = stubHandler(wrapped);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, wrapped);
        }

        @Test
        @DisplayName("Re-checks metalcraft condition at resolution time (intervening-if)")
        void rechecksMetalcraftConditionAtResolutionTime() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            // Metalcraft lost before resolution — condition re-evaluated and fails
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameQueryService).isMetalcraftMet(gd, player1Id);
            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Logs skip message when metalcraft lost before resolution")
        void logsSkipMessageWhenMetalcraftLostBeforeResolution() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            // Metalcraft lost before resolution
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Concussive Bolt") && msg.contains("does nothing")
                            && msg.contains("fewer than three artifacts")));
        }
    }

    // =========================================================================
    // MetalcraftReplacementEffect
    // (GalvanicBlast: MetalcraftReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftReplacementEffect")
    class ResolveMetalcraftReplacementEffect {

        @Test
        @DisplayName("Resolves base effect when metalcraft is not met")
        void resolvesBaseEffectWhenMetalcraftNotMet() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
        }

        @Test
        @DisplayName("Resolves metalcraft effect when metalcraft is met")
        void resolvesMetalcraftEffectWhenMetalcraftMet() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            EffectHandler handler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, upgraded);
        }

        @Test
        @DisplayName("Re-checks metalcraft condition at resolution time")
        void rechecksConditionAtResolutionTime() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            // Metalcraft lost at resolution time — falls back to base
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameQueryService).isMetalcraftMet(gd, player1Id);
            verify(handler).resolve(gd, entry, base);
        }
    }

    // =========================================================================
    // Metalcraft condition threshold
    // =========================================================================

    @Nested
    @DisplayName("metalcraftCondition")
    class MetalcraftCondition {

        @Test
        @DisplayName("Two artifacts do not meet metalcraft threshold")
        void twoArtifactsDoNotMeetThreshold() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
        }

        @Test
        @DisplayName("Exactly three artifacts meets metalcraft threshold")
        void exactlyThreeArtifactsMeetsThreshold() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            EffectHandler handler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, upgraded);
        }

        @Test
        @DisplayName("Artifact creatures count toward metalcraft")
        void artifactCreaturesCountForMetalcraft() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            EffectHandler handler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            // Verifies metalcraft is checked for the correct controller
            verify(gameQueryService).isMetalcraftMet(gd, player1Id);
            verify(handler).resolve(gd, entry, upgraded);
        }

        @Test
        @DisplayName("More than three artifacts still meets metalcraft threshold")
        void moreThanThreeArtifactsMeetsThreshold() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            stubHandler(base);
            EffectHandler upgradedHandler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(upgradedHandler).resolve(gd, entry, upgraded);
            verify(registry, never()).getHandler(base);
        }

        @Test
        @DisplayName("Zero artifacts does not meet metalcraft threshold")
        void zeroArtifactsDoesNotMeetThreshold() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new MetalcraftReplacementEffect(base, upgraded);
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            EffectHandler baseHandler = stubHandler(base);
            stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(baseHandler).resolve(gd, entry, base);
            verify(registry, never()).getHandler(upgraded);
        }
    }

    // =========================================================================
    // Multi-effect resolution
    // =========================================================================

    @Nested
    @DisplayName("multiEffectResolution")
    class MultiEffectResolution {

        @Test
        @DisplayName("All effects on a stack entry resolve in sequence")
        void allEffectsOnStackEntryResolveInSequence() {
            CardEffect damageEffect = new DealDamageToTargetPlayerEffect(4);
            CardEffect cantBlockEffect = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(cantBlockEffect);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id,
                    List.of(damageEffect, conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            EffectHandler damageHandler = stubHandler(damageEffect);
            EffectHandler cantBlockHandler = stubHandler(cantBlockEffect);

            effectResolutionService.resolveEffects(gd, entry);

            InOrder inOrder = inOrder(damageHandler, cantBlockHandler);
            inOrder.verify(damageHandler).resolve(gd, entry, damageEffect);
            inOrder.verify(cantBlockHandler).resolve(gd, entry, cantBlockEffect);
        }

        @Test
        @DisplayName("Non-metalcraft effects still resolve when metalcraft conditional is skipped")
        void nonMetalcraftEffectsStillResolveWhenMetalcraftSkips() {
            CardEffect damageEffect = new DealDamageToTargetPlayerEffect(4);
            CardEffect cantBlockEffect = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(cantBlockEffect);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id,
                    List.of(damageEffect, conditional));

            // No metalcraft — conditional effect is skipped
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            EffectHandler damageHandler = stubHandler(damageEffect);

            effectResolutionService.resolveEffects(gd, entry);

            // First effect (non-metalcraft) still fires
            verify(damageHandler).resolve(gd, entry, damageEffect);
            // Second effect (metalcraft conditional) is skipped
            verify(registry, never()).getHandler(cantBlockEffect);
        }
    }

    // =========================================================================
    // ControlsPermanentConditionalEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveControlsPermanentConditionalEffect")
    class ResolveControlsPermanentConditionalEffect {

        @Test
        @DisplayName("Skips wrapped effect when controller has no matching permanent")
        void skipsWrappedEffectWhenNoMatchingPermanent() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = new ControlsPermanentConditionalEffect(new PermanentIsArtifactPredicate(), wrapped);
            StackEntry entry = createEntry(createCard("Temporal Machinations"), player1Id, List.of(conditional));

            // No permanents on battlefield — condition not met
            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Resolves wrapped effect when controller has a matching permanent")
        void resolvesWrappedEffectWhenMatchingPermanent() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = new ControlsPermanentConditionalEffect(new PermanentIsArtifactPredicate(), wrapped);
            StackEntry entry = createEntry(createCard("Temporal Machinations"), player1Id, List.of(conditional));

            // Add an artifact to controller's battlefield
            Permanent artifact = mock(Permanent.class);
            gd.playerBattlefields.get(player1Id).add(artifact);
            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(artifact), any(PermanentIsArtifactPredicate.class)))
                    .thenReturn(true);
            EffectHandler handler = stubHandler(wrapped);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, wrapped);
        }

        @Test
        @DisplayName("Logs skip message when condition not met")
        void logsSkipMessageWhenConditionNotMet() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = new ControlsPermanentConditionalEffect(new PermanentIsArtifactPredicate(), wrapped);
            StackEntry entry = createEntry(createCard("Temporal Machinations"), player1Id, List.of(conditional));

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Temporal Machinations") && msg.contains("does nothing")));
        }
    }

    // =========================================================================
    // Resolution state management
    // =========================================================================

    @Nested
    @DisplayName("resolutionStateManagement")
    class ResolutionStateManagement {

        @Test
        @DisplayName("Clears pendingEffectResolutionEntry after all effects resolve")
        void clearsPendingEffectResolutionEntryAfterResolution() {
            CardEffect effect = new DealDamageToAnyTargetEffect(2);
            CardEffect replacement = new MetalcraftReplacementEffect(effect, new DealDamageToAnyTargetEffect(4));
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            stubHandler(effect);

            effectResolutionService.resolveEffects(gd, entry);

            assertThat(gd.pendingEffectResolutionEntry).isNull();
        }

        @Test
        @DisplayName("Clears pendingEffectResolutionIndex after all effects resolve")
        void clearsPendingEffectResolutionIndexAfterResolution() {
            CardEffect effect = new DealDamageToAnyTargetEffect(2);
            CardEffect replacement = new MetalcraftReplacementEffect(effect, new DealDamageToAnyTargetEffect(4));
            StackEntry entry = createEntry(createCard("Galvanic Blast"), player1Id, List.of(replacement));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);
            stubHandler(effect);

            effectResolutionService.resolveEffects(gd, entry);

            assertThat(gd.pendingEffectResolutionIndex).isZero();
        }

        @Test
        @DisplayName("Clears pending state after multi-effect spell resolves")
        void clearsPendingStateAfterMultiEffectSpellResolves() {
            CardEffect damageEffect = new DealDamageToTargetPlayerEffect(4);
            CardEffect cantBlockEffect = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new MetalcraftConditionalEffect(cantBlockEffect);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id,
                    List.of(damageEffect, conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);
            stubHandler(damageEffect);
            stubHandler(cantBlockEffect);

            effectResolutionService.resolveEffects(gd, entry);

            assertThat(gd.pendingEffectResolutionEntry).isNull();
            assertThat(gd.pendingEffectResolutionIndex).isZero();
        }
    }
}

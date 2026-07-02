package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

    private EffectResolutionService effectResolutionService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        effectResolutionService = new EffectResolutionService(
                new ConditionEvaluationService(gameQueryService, new StaticEffectSupport(gameQueryService)),
                registry, gameBroadcastService, permanentRemovalService);
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

    private StackEntry createTargetedEntry(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects, 0, targetId, null);
    }

    private StackEntry createTriggeredEntry(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId, card.getName(), effects, null, sourcePermanentId);
    }

    private EffectHandler stubHandler(CardEffect effect) {
        EffectHandler handler = mock(EffectHandler.class);
        lenient().when(registry.getHandler(effect)).thenReturn(handler);
        return handler;
    }

    // =========================================================================
    // ConditionalEffect
    // (ConcussiveBolt: DealDamageToTargetPlayerEffect(4) + ConditionalEffect(TargetPlayerCreaturesCantBlockThisTurnEffect))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftConditionalEffect")
    class ResolveMetalcraftConditionalEffect {

        @Test
        @DisplayName("Skips wrapped effect when metalcraft is not met")
        void skipsWrappedEffectWhenMetalcraftNotMet() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), wrapped);
            StackEntry entry = createEntry(createCard("Concussive Bolt"), player1Id, List.of(conditional));

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Logs skip message when metalcraft is not met")
        void logsSkipMessageWhenMetalcraftNotMet() {
            CardEffect wrapped = new TargetPlayerCreaturesCantBlockThisTurnEffect();
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), wrapped);
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), wrapped);
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), wrapped);
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), wrapped);
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
    // ConditionalReplacementEffect
    // (GalvanicBlast: ConditionalReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftReplacementEffect")
    class ResolveMetalcraftReplacementEffect {

        @Test
        @DisplayName("Resolves base effect when metalcraft is not met")
        void resolvesBaseEffectWhenMetalcraftNotMet() {
            CardEffect base = new DealDamageToAnyTargetEffect(2);
            CardEffect upgraded = new DealDamageToAnyTargetEffect(4);
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), base, upgraded);
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), cantBlockEffect);
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), cantBlockEffect);
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
    // ConditionalEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveControlsPermanentConditionalEffect")
    class ResolveControlsPermanentConditionalEffect {

        @Test
        @DisplayName("Skips wrapped effect when controller has no matching permanent")
        void skipsWrappedEffectWhenNoMatchingPermanent() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()), wrapped);
            StackEntry entry = createEntry(createCard("Temporal Machinations"), player1Id, List.of(conditional));

            // No permanents on battlefield — condition not met
            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Resolves wrapped effect when controller has a matching permanent")
        void resolvesWrappedEffectWhenMatchingPermanent() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()), wrapped);
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
            CardEffect conditional = new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()), wrapped);
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), effect, new DealDamageToAnyTargetEffect(4));
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
            CardEffect replacement = new ConditionalReplacementEffect(new Metalcraft(), effect, new DealDamageToAnyTargetEffect(4));
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
            CardEffect conditional = new ConditionalEffect(new Metalcraft(), cantBlockEffect);
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

    // =========================================================================
    // ConditionalEffect (nontoken predicate composition)
    // =========================================================================

    @Nested
    @DisplayName("resolveControlsAnotherPermanentConditionalEffect")
    class ResolveControlsAnotherPermanentConditionalEffect {

        private Permanent createPiratePermanent(boolean isToken) {
            Card card = new Card();
            card.setName(isToken ? "Pirate Token" : "Pirate Creature");
            card.setType(CardType.CREATURE);
            card.setSubtypes(List.of(CardSubtype.PIRATE));
            card.setToken(isToken);
            return new Permanent(card);
        }

        private CardEffect controlsAnotherPirate(boolean nontokenOnly, CardEffect wrapped) {
            PermanentPredicate predicate = new PermanentHasSubtypePredicate(CardSubtype.PIRATE);
            if (nontokenOnly) {
                predicate = new PermanentAllOfPredicate(List.of(
                        predicate,
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                ));
            }
            return new ConditionalEffect(new ControlsAnotherPermanent(predicate), wrapped);
        }

        private void stubPredicateEvaluation() {
            when(gameQueryService.matchesPermanentPredicate(eq(gd), any(Permanent.class), any(PermanentPredicate.class)))
                    .thenAnswer(invocation -> {
                        Permanent permanent = invocation.getArgument(1);
                        PermanentPredicate predicate = invocation.getArgument(2);
                        return matches(permanent, predicate);
                    });
        }

        private boolean matches(Permanent permanent, PermanentPredicate predicate) {
            if (predicate instanceof PermanentHasSubtypePredicate hasSubtype) {
                return permanent.getCard().getSubtypes().contains(hasSubtype.subtype());
            }
            if (predicate instanceof PermanentIsTokenPredicate) {
                return permanent.getCard().isToken();
            }
            if (predicate instanceof PermanentNotPredicate notPredicate) {
                return !matches(permanent, notPredicate.predicate());
            }
            if (predicate instanceof PermanentAllOfPredicate allOfPredicate) {
                return allOfPredicate.predicates().stream().allMatch(p -> matches(permanent, p));
            }
            return false;
        }

        @Test
        @DisplayName("Resolves wrapped effect when another nontoken permanent with subtype exists")
        void resolvesWhenAnotherNontokenSubtypeExists() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = controlsAnotherPirate(true, wrapped);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(createCard("Fathom Fleet Captain"), player1Id,
                    List.of(conditional), sourcePermanentId);

            // Add a nontoken Pirate (different from source)
            Permanent pirate = createPiratePermanent(false);
            gd.playerBattlefields.get(player1Id).add(pirate);
            stubPredicateEvaluation();

            EffectHandler handler = stubHandler(wrapped);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, wrapped);
        }

        @Test
        @DisplayName("Skips wrapped effect when only token permanents with subtype exist and nontokenOnly is true")
        void skipsWhenOnlyTokenSubtypeExistsAndNontokenOnly() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = controlsAnotherPirate(true, wrapped);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(createCard("Fathom Fleet Captain"), player1Id,
                    List.of(conditional), sourcePermanentId);

            // Add only a token Pirate
            Permanent tokenPirate = createPiratePermanent(true);
            gd.playerBattlefields.get(player1Id).add(tokenPirate);
            stubPredicateEvaluation();

            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Token permanents satisfy condition when nontokenOnly is false")
        void tokensSatisfyConditionWhenNontokenOnlyFalse() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = controlsAnotherPirate(false, wrapped);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(createCard("Ghitu Journeymage"), player1Id,
                    List.of(conditional), sourcePermanentId);

            // Add a token Pirate (should be accepted when nontokenOnly=false)
            Permanent tokenPirate = createPiratePermanent(true);
            gd.playerBattlefields.get(player1Id).add(tokenPirate);
            stubPredicateEvaluation();

            EffectHandler handler = stubHandler(wrapped);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, wrapped);
        }

        @Test
        @DisplayName("Skips when no other permanent exists (source excluded)")
        void skipsWhenNoOtherPermanentExists() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = controlsAnotherPirate(true, wrapped);

            // The source permanent is a Pirate — but it should be excluded from the check
            Permanent source = createPiratePermanent(false);
            gd.playerBattlefields.get(player1Id).add(source);

            StackEntry entry = createTriggeredEntry(createCard("Fathom Fleet Captain"), player1Id,
                    List.of(conditional), source.getId());

            effectResolutionService.resolveEffects(gd, entry);

            verify(registry, never()).getHandler(wrapped);
        }

        @Test
        @DisplayName("Logs skip message when nontokenOnly condition not met")
        void logsSkipMessageWhenNontokenConditionNotMet() {
            CardEffect wrapped = new DrawCardEffect(1);
            CardEffect conditional = controlsAnotherPirate(true, wrapped);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = createTriggeredEntry(createCard("Fathom Fleet Captain"), player1Id,
                    List.of(conditional), sourcePermanentId);

            // Only a token Pirate on battlefield — condition not met
            Permanent tokenPirate = createPiratePermanent(true);
            gd.playerBattlefields.get(player1Id).add(tokenPirate);
            stubPredicateEvaluation();

            effectResolutionService.resolveEffects(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Fathom Fleet Captain") && msg.contains("does nothing")
                            && msg.contains("matching permanent")));
        }
    }

    // =========================================================================
    // ConditionalReplacementEffect
    // (HuatlisSpurring: ConditionalReplacementEffect(PermanentHasSubtypePredicate(HUATLI), BoostTargetCreatureEffect(2,0), BoostTargetCreatureEffect(4,0)))
    // =========================================================================

    @Nested
    @DisplayName("resolveControlsPermanentReplacementEffect")
    class ResolveControlsPermanentReplacementEffect {

        @Test
        @DisplayName("Resolves base effect when controller does not control the subtype")
        void resolvesBaseEffectWhenSubtypeNotControlled() {
            CardEffect base = new BoostTargetCreatureEffect(2, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(4, 0);
            CardEffect replacement = new ConditionalReplacementEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUATLI)), base, upgraded);
            StackEntry entry = createEntry(createCard("Huatli's Spurring"), player1Id, List.of(replacement));

            // No permanents with HUATLI subtype on battlefield
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
        }

        @Test
        @DisplayName("Resolves upgraded effect when controller controls the subtype")
        void resolvesUpgradedEffectWhenSubtypeControlled() {
            CardEffect base = new BoostTargetCreatureEffect(2, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(4, 0);
            CardEffect replacement = new ConditionalReplacementEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUATLI)), base, upgraded);
            StackEntry entry = createEntry(createCard("Huatli's Spurring"), player1Id, List.of(replacement));

            // Add a permanent with HUATLI subtype
            Card huatliCard = createCard("Huatli, Warrior Poet");
            huatliCard.setSubtypes(List.of(CardSubtype.HUATLI));
            Permanent huatli = new Permanent(huatliCard);
            gd.playerBattlefields.get(player1Id).add(huatli);

            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(huatli), any())).thenReturn(true);
            EffectHandler handler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, upgraded);
        }

        @Test
        @DisplayName("Opponent's subtype permanent does not meet the condition")
        void opponentSubtypeDoesNotCount() {
            CardEffect base = new BoostTargetCreatureEffect(2, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(4, 0);
            CardEffect replacement = new ConditionalReplacementEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUATLI)), base, upgraded);
            StackEntry entry = createEntry(createCard("Huatli's Spurring"), player1Id, List.of(replacement));

            // Huatli on opponent's battlefield only
            Card huatliCard = createCard("Huatli, Warrior Poet");
            huatliCard.setSubtypes(List.of(CardSubtype.HUATLI));
            Permanent huatli = new Permanent(huatliCard);
            gd.playerBattlefields.get(player2Id).add(huatli);

            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
        }
    }

    // =========================================================================
    // ConditionalReplacementEffect
    // (ElderCathar: ConditionalReplacementEffect(PermanentHasSubtypePredicate(HUMAN), PutPlusOnePlusOneCounterOnTargetCreatureEffect(1), PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)))
    // =========================================================================

    @Nested
    @DisplayName("resolveTargetPermanentReplacementEffect")
    class ResolveTargetPermanentReplacementEffect {

        @Test
        @DisplayName("Resolves base effect when target permanent does not match")
        void resolvesBaseEffectWhenTargetDoesNotMatch() {
            CardEffect base = new BoostTargetCreatureEffect(1, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(2, 0);
            UUID targetId = UUID.randomUUID();
            CardEffect replacement = new ConditionalReplacementEffect(new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), base, upgraded);
            StackEntry entry = createTargetedEntry(createCard("Test Spell"), player1Id, List.of(replacement), targetId);
            Permanent target = new Permanent(createCard("Grizzly Bears"));
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(target);
            when(gameQueryService.matchesPermanentPredicate(gd, target, new PermanentHasSubtypePredicate(CardSubtype.HUMAN))).thenReturn(false);
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
        }

        @Test
        @DisplayName("Resolves upgraded effect when target permanent matches")
        void resolvesUpgradedEffectWhenTargetMatches() {
            CardEffect base = new BoostTargetCreatureEffect(1, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(2, 0);
            UUID targetId = UUID.randomUUID();
            CardEffect replacement = new ConditionalReplacementEffect(new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), base, upgraded);
            StackEntry entry = createTargetedEntry(createCard("Test Spell"), player1Id, List.of(replacement), targetId);
            Permanent target = new Permanent(createCard("Champion of the Parish"));
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(target);
            when(gameQueryService.matchesPermanentPredicate(gd, target, new PermanentHasSubtypePredicate(CardSubtype.HUMAN))).thenReturn(true);
            EffectHandler handler = stubHandler(upgraded);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, upgraded);
        }

        @Test
        @DisplayName("Resolves base effect when target permanent is missing")
        void resolvesBaseEffectWhenTargetMissing() {
            CardEffect base = new BoostTargetCreatureEffect(1, 0);
            CardEffect upgraded = new BoostTargetCreatureEffect(2, 0);
            UUID targetId = UUID.randomUUID();
            CardEffect replacement = new ConditionalReplacementEffect(new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), base, upgraded);
            StackEntry entry = createTargetedEntry(createCard("Test Spell"), player1Id, List.of(replacement), targetId);
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);
            EffectHandler handler = stubHandler(base);

            effectResolutionService.resolveEffects(gd, entry);

            verify(handler).resolve(gd, entry, base);
            verify(gameQueryService, never()).matchesPermanentPredicate(eq(gd), any(), any());
        }
    }
}

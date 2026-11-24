package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.replacement.DamageJournalService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class AbstractDamageHandlerTest {

    @Mock protected GraveyardService graveyardService;
    @Mock protected DamagePreventionService damagePreventionService;
    @Mock protected GameOutcomeService gameOutcomeService;
    @Mock protected GameQueryService gameQueryService;
    @Mock protected PredicateEvaluationService predicateEvaluationService;
    @Mock protected GameLogService gameLogService;
    @Mock protected PermanentRemovalService permanentRemovalService;
    @Mock protected com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers damageTriggers;
    @Mock protected LifeSupport lifeSupport;
    @Mock protected com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;
    @Mock protected BattleDefeatSupport battleDefeatSupport;
    /** Real journal: it keeps its state on {@link GameData}, so with no journal open every event is untracked. */
    @Spy protected DamageJournalService damageJournalService = new DamageJournalService();
    @Mock protected ReplacementOrderService replacementOrderService;
    @Mock protected PermanentControlSupport permanentControlSupport;
    @Mock protected PermanentCounterSupport permanentCounterSupport;
    @Mock protected TapUntapSupport tapUntapSupport;
    @Mock protected VisualCueService visualCueService;

    @InjectMocks protected DamageSupport damageSupport;

    /** Real evaluator over the mocked collaborators — Fixed/XValue amounts never touch the mocks. */
    protected AmountEvaluationService amountEvaluationService;

    protected GameData gd;
    protected UUID player1Id;
    protected UUID player2Id;

    @BeforeEach
    void setUpDamageHandlerBase() {
        var game = EffectHandlerTestFixtures.newTwoPlayerGameData(true);
        player1Id = game.player1Id();
        player2Id = game.player2Id();
        gd = game.gameData();
        amountEvaluationService = new AmountEvaluationService(predicateEvaluationService, gameQueryService);
        // Field-injected (@Lazy) in production, so constructor injection leaves it out.
        org.springframework.test.util.ReflectionTestUtils.setField(damageSupport, "damageTriggers", damageTriggers);
        // The production rule: the source permanent's controller (looked up on the battlefields), else its last
        // known controller, else the entry's.
        lenient().when(gameQueryService.findDamageSourceController(eq(gd), any(), any())).thenAnswer(inv -> {
            StackEntry entry = inv.getArgument(1);
            Permanent source = inv.getArgument(2);
            UUID controllerId = source == null ? null : gd.playerBattlefields.entrySet().stream()
                    .filter(battlefield -> battlefield.getValue().contains(source))
                    .map(java.util.Map.Entry::getKey).findFirst().orElse(null);
            if (controllerId == null && source != null && entry != null) {
                controllerId = entry.lastKnownPermanentController(source.getId());
            }
            return controllerId != null || entry == null ? controllerId : entry.getControllerId();
        });
        // No replacement or prevention effect applies in these unit tests: every damage event completes unchanged.
        lenient().when(replacementOrderService.run(eq(gd), any()))
                .thenAnswer(inv -> new ReplacementOrderService.Outcome.Completed<>(inv.getArgument(1)));
        lenient().when(gameQueryService.getEnchantedPlayerDamageMultiplier(eq(gd), any(UUID.class))).thenReturn(1);
        lenient().when(gameQueryService.getDamageToRecipientMultiplier(eq(gd), any(), any())).thenReturn(1);
        lenient().when(gameQueryService.getDamageToRecipientMultiplier(eq(gd), any(), any(), any())).thenReturn(1);
        lenient().when(gameQueryService.getPermanentDamageMultiplier(eq(gd), any(UUID.class))).thenReturn(1);
        lenient().when(gameQueryService.getSourceDamageMultiplier(eq(gd), any(), any(Permanent.class))).thenReturn(1);
        lenient().when(gameQueryService.opponentLifeLossMultiplier(eq(gd), any(UUID.class))).thenReturn(1);
        lenient().when(gameQueryService.hasProtectionFromDamageSource(
                eq(gd), any(Permanent.class), any(Card.class), any())).thenReturn(false);
        lenient().when(gameQueryService.applyDamageReplacementEffects(eq(gd), anyInt()))
                .thenAnswer(inv -> inv.getArgument(1));
        lenient().when(gameQueryService.applyDamageReplacementEffects(
                        eq(gd), any(StackEntry.class), any(UUID.class), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(gameQueryService.applyOjerAxonilDamageReplacement(
                        eq(gd), anyInt(), any(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        // Reflect Damage is a pass-through when no replacement shield is set up.
        lenient().when(damagePreventionService.applyReflectDamageToSourceControllerShield(
                        eq(gd), nullable(UUID.class), anyInt()))
                .thenAnswer(inv -> inv.getArgument(2));
        // Opal-Eye redirect is likewise a pass-through when no shield is set up.
        lenient().when(damagePreventionService.applySourceNextDamageRedirectToPermanent(
                        eq(gd), nullable(UUID.class), nullable(UUID.class), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applySourcePermanentAndControllerNextDamageRedirectToPermanent(
                        eq(gd), any(), nullable(UUID.class), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applySourcePermanentAndControllerNextDamageRedirectToPlayer(
                        eq(gd), any(), nullable(UUID.class), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        // Palisade Giant / RedirectPlayerDamageToSelfEffect (includeOtherPermanents): pass-through when no
        // absorbing permanent is set up. Called unconditionally for creature damage once the controller is known.
        lenient().when(damagePreventionService.applyStaticPermanentDamageRedirectToSelf(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyAllCreatureDamageRedirectToController(
                        eq(gd), any(Permanent.class), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        // Martyrdom redirect is likewise a pass-through — dealDamageToPlayer calls it unconditionally.
        lenient().when(damagePreventionService.applyPlayerNextDamageRedirectShields(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        // Oracle's Attendants: consulted for every creature damage event, including spell damage with no
        // source permanent, so it must pass through when no redirect shield is set up.
        lenient().when(damagePreventionService.applyCreatureRedirectShields(eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyEnchantedCreatureDamageRedirectToController(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applySourceRedirectShields(eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyPlayerSourceNextDamageRedirectShield(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyPlayerNextSourceDamageShield(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyPlayerNextSourceDamageShield(
                        eq(gd), any(), any(), anyInt(), anyBoolean(), nullable(Card.class)))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyTargetSourcePreventionShield(eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(
                        eq(gd), any(), anyInt(), any()))
                .thenAnswer(inv -> inv.getArgument(2));
        lenient().when(damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(
                        eq(gd), any(), anyInt(), any(), any(StackEntry.class)))
                .thenAnswer(inv -> inv.getArgument(2));
        lenient().when(damagePreventionService.applyControllerCreaturesNextSourceDamageShield(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyChannelHarmPrevention(
                        eq(gd), any(), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyChannelHarmPreventionToPermanent(
                        eq(gd), any(Permanent.class), any(), anyInt()))
                .thenAnswer(inv -> inv.getArgument(3));
        lenient().when(damagePreventionService.applyComeuppancePrevention(
                        eq(gd), any(), anyInt(), any(), any(), any(), anyBoolean()))
                .thenAnswer(inv -> inv.getArgument(2));
        lenient().when(damagePreventionService.applyJudgmentOfAlexanderPrevention(
                        eq(gd), any(), anyInt(), any(), any(), any(), anyBoolean()))
                .thenAnswer(inv -> inv.getArgument(2));
        setUpHandler();
    }

    protected abstract void setUpHandler();

    protected static Card createCard(String name) {
        return EffectHandlerTestFixtures.createInstantCard(name);
    }

    protected static Card createCreature(String name, int power, int toughness) {
        return EffectHandlerTestFixtures.createCreatureCard(name, power, toughness);
    }

    protected Permanent addPermanent(UUID playerId, Card card) {
        return EffectHandlerTestFixtures.addPermanent(gd, playerId, card);
    }

    protected StackEntry createEntry(Card card, UUID controllerId, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(), List.of());
        entry.setTargetId(targetId);
        return entry;
    }

    protected StackEntry createEntryWithXValue(Card card, UUID controllerId, int xValue, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), xValue);
        entry.setTargetId(targetId);
        return entry;
    }

    protected StackEntry createMultiTargetEntry(Card card, UUID controllerId, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), 0, targetIds);
    }

    /**
     * Damage can be prevented in this game. Lenient: only some damage paths still ask (protection and source-wide
     * prevention); the prevention effects themselves are replacement candidates, mocked away here.
     */
    protected void stubDamagePreventable() {
        lenient().when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
    }

    protected void stubDamageFromSourceNotPrevented() {
        when(gameQueryService.isDamageFromStackEntryPrevented(eq(gd), any())).thenReturn(false);
    }

    protected void stubNoDamageMultiplier() {
        lenient().when(gameQueryService.getDamageToRecipientMultiplier(
                eq(gd), any(UUID.class), nullable(UUID.class), any(UUID.class))).thenReturn(1);
    }

    protected void stubCreatureDamageCore(Permanent target, int toughness) {
        // Divided damage skips permanents that are no longer creatures/planeswalkers at
        // resolution (CR 608.2b), so the guard must see the target as a creature.
        lenient().when(gameQueryService.isCreature(gd, target)).thenReturn(true);
        lenient().when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        // Lenient: record-only damage no longer reads toughness — destruction happens at the
        // SBA check, outside these handler unit tests. Excess-damage paths still read it.
        lenient().when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(toughness);
        lenient().when(gameQueryService.getLethalDamageThreshold(gd, target)).thenReturn(toughness);
    }

    protected void stubNoKeywordsOnSource(StackEntry entry) {
        // The infect/wither decision goes through sourceDealsCounterDamageToCreatures now,
        // whose mock default (false) is the wanted answer — only deathtouch needs a stub.
        // Lenient: the deathtouch check is skipped when the damage was fully prevented (CR 702.2b).
        lenient().when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    protected void stubNoKeywordsOnSourceWithDamageSource(StackEntry entry, Permanent damageSource) {
        lenient().when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), eq(damageSource), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    protected void stubNoInfectOnSource(StackEntry entry) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.INFECT))).thenReturn(false);
    }

    protected void stubPlayerDamageCore(UUID playerId) {
        when(gameQueryService.canPlayerLoseLife(gd, playerId)).thenReturn(true);
        when(gameQueryService.shouldDamageBeDealtAsInfect(gd, playerId)).thenReturn(false);
    }

    /** The damage rules events the handler emitted, in order. */
    protected java.util.List<com.github.laxika.magicalvibes.model.rules.DamageDealtEvent> damageEvents() {
        return gd.rulesEvents.pending().stream().flatMap(batch -> batch.events().stream())
                .filter(com.github.laxika.magicalvibes.model.rules.DamageDealtEvent.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.rules.DamageDealtEvent.class::cast).toList();
    }

    /** Asserts the handler emitted the damage event of {@code damage} dealt to {@code target} by the source named. */
    protected void assertDamageToPermanent(Permanent target, int damage, UUID sourceControllerId, Card sourceCard,
                                           UUID sourceId) {
        org.assertj.core.api.Assertions.assertThat(damageEvents()).anySatisfy(event ->
                org.assertj.core.api.Assertions.assertThat(event)
                        .isInstanceOfSatisfying(com.github.laxika.magicalvibes.model.rules.DamageDealtEvent.ToPermanent.class,
                                dealt -> {
                                    org.assertj.core.api.Assertions.assertThat(dealt.target()).isSameAs(target);
                                    org.assertj.core.api.Assertions.assertThat(dealt.damage()).isEqualTo(damage);
                                    org.assertj.core.api.Assertions.assertThat(dealt.sourceControllerId())
                                            .isEqualTo(sourceControllerId);
                                    org.assertj.core.api.Assertions.assertThat(dealt.sourceCard()).isSameAs(sourceCard);
                                    org.assertj.core.api.Assertions.assertThat(dealt.sourceId()).isEqualTo(sourceId);
                                }));
    }

    /** Asserts the handler emitted the damage event of {@code damage} dealt to the player {@code playerId}. */
    protected void assertDamageToPlayer(UUID playerId, int damage) {
        org.assertj.core.api.Assertions.assertThat(damageEvents()).anySatisfy(event ->
                org.assertj.core.api.Assertions.assertThat(event)
                        .isInstanceOfSatisfying(com.github.laxika.magicalvibes.model.rules.DamageDealtEvent.ToPlayer.class,
                                dealt -> {
                                    org.assertj.core.api.Assertions.assertThat(dealt.playerId()).isEqualTo(playerId);
                                    org.assertj.core.api.Assertions.assertThat(dealt.damage()).isEqualTo(damage);
                                }));
    }
}

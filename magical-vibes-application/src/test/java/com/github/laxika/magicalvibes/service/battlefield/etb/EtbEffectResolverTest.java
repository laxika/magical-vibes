package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EntryFacts;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EtbEffectResolver} and the trigger-time form of an entering permanent's own "when this enters"
 * effects ({@code CardEffect.asItTriggersOnEntering}), judged with the facts the resolver's context gives: a fact fixed
 * as the permanent entered is judged as the ability triggers, any other condition as it resolves.
 */
@ExtendWith(MockitoExtension.class)
class EtbEffectResolverTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;

    private EtbEffectResolver resolver;
    private GameData gameData;
    private Card card;
    private UUID controllerId;

    @BeforeEach
    void setUp() {
        resolver = new EtbEffectResolver(new ConditionEvaluationService(gameQueryService, predicateEvaluationService));
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player1");
        card = new Card();
        card.setName("Test Creature");
    }

    /** The facts of {@code ctx}'s entering permanent, judged as the stack door judges them. */
    private EntryFacts facts(EtbEffectContext ctx) {
        ConditionEvaluationService conditions = new ConditionEvaluationService(gameQueryService,
                predicateEvaluationService);
        return new EntryFacts() {
            @Override
            public boolean isMet(Condition condition) {
                return conditions.isMet(ctx.gameData(), condition, resolver.entryContext(ctx));
            }

            @Override
            public boolean evoked() {
                return ctx.evoked();
            }

            @Override
            public boolean tributePaid() {
                return ctx.sourcePermanent() != null && ctx.sourcePermanent().isTributePaid();
            }
        };
    }

    private EtbEffectContext ctx(boolean wasCastFromHand, int etbMode, boolean kicked) {
        return new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode, kicked, false, false);
    }

    @Test
    @DisplayName("Unregistered effect passes through unchanged (default identity)")
    void defaultIdentity() {
        DrawCardEffect draw = new DrawCardEffect(1);
        assertThat(draw.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(draw);
    }

    @Test
    void resolutionOnlyConditionIsNotEvaluatedDuringEntry() {
        CardEffect effect = new ConditionalEffect(new Metalcraft(), new DrawCardEffect(1));

        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("\"If you didn't cast it from your hand\" is an entry fact: judged as the ability triggers, as written")
    void notCastFromHandInterveningIf() {
        ControllerLosesGameEffect lose = new ControllerLosesGameEffect();
        NotCondition notFromHand = new NotCondition(new CastFromZone(Zone.HAND));
        ConditionalEffect phage = new ConditionalEffect(notFromHand, lose);

        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), notFromHand)).isFalse();
        assertThat(resolver.ownEntryConditionMet(ctx(false, 0, false), notFromHand)).isTrue();
        assertThat(phage.asItTriggersOnEntering(facts(ctx(false, 0, false)))).isSameAs(lose);
    }

    @Test
    @DisplayName("KickedConditional: unwraps when kicked, does nothing otherwise")
    void kickedConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect kicked = new ConditionalEffect(new Kicked(), wrapped);

        assertThat(kicked.asItTriggersOnEntering(facts(ctx(true, 0, true)))).isSameAs(wrapped);
        assertThat(kicked.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("CastForMadnessCost: unwraps only when madness was paid")
    void castForMadnessCostConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect madness = new ConditionalEffect(new CastForMadnessCost(), wrapped);

        EtbEffectContext madnessContext = new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, true, false, null, List.of());
        EtbEffectContext normalContext = new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, false, false, null, List.of());
        assertThat(madness.asItTriggersOnEntering(facts(madnessContext))).isSameAs(wrapped);
        assertThat(madness.asItTriggersOnEntering(facts(normalContext))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("CastFromZoneConditional(HAND): unwraps only when cast from hand")
    void castFromHandConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect fromHand = new ConditionalEffect(new CastFromZone(Zone.HAND), wrapped);

        assertThat(fromHand.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(wrapped);
        assertThat(fromHand.asItTriggersOnEntering(facts(ctx(false, 0, false)))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("ColorSpentToCast: snapshots the cast-time result into the ETB trigger")
    void colorSpentToCastConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect twoWhite = new ConditionalEffect(
                new ColorSpentToCast(com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2), wrapped);
        java.util.EnumMap<com.github.laxika.magicalvibes.model.ManaColor, Integer> spent =
                new java.util.EnumMap<>(com.github.laxika.magicalvibes.model.ManaColor.class);

        spent.put(com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2);
        gameData.setSpellCastManaSpentByColor(card.getId(), spent);
        assertThat(twoWhite.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(wrapped);

        spent.put(com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        gameData.setSpellCastManaSpentByColor(card.getId(), spent);
        assertThat(twoWhite.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("SnowManaSpentToCast: snapshots the cast-time result into the ETB trigger")
    void snowManaSpentToCastConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect snowMana = new ConditionalEffect(new SnowManaSpentToCast(), wrapped);

        gameData.setSpellCastSnowManaSpent(card.getId(), 1);
        assertThat(snowMana.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(wrapped);

        gameData.setSpellCastSnowManaSpent(card.getId(), 0);
        assertThat(snowMana.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("WasCastConditional: unwraps for a cast permanent but does nothing for a battlefield entry")
    void wasCastConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect wasCast = new ConditionalEffect(new WasCast(), wrapped);
        Permanent source = new Permanent(card);

        source.setCast(true);
        assertThat(wasCast.asItTriggersOnEntering(facts(new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, source)))).isSameAs(wrapped);

        source.setCast(false);
        assertThat(wasCast.asItTriggersOnEntering(facts(new EtbEffectContext(gameData, card, controllerId,
                false, 0, false, false, false, source)))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("Metalcraft intervening if: judged as the ability triggers, kept on the effect to be checked again as it resolves")
    void metalcraftGate() {
        ConditionalEffect effect = new ConditionalEffect(new Metalcraft(), new DrawCardEffect(1));

        when(gameQueryService.isMetalcraftMet(gameData, controllerId)).thenReturn(true);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isTrue();

        when(gameQueryService.isMetalcraftMet(gameData, controllerId)).thenReturn(false);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isFalse();
        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("Morbid intervening if: judged as the ability triggers, kept on the effect to be checked again as it resolves")
    void morbidGate() {
        ConditionalEffect effect = new ConditionalEffect(new Morbid(), new DrawCardEffect(1));

        when(gameQueryService.isMorbidMet(gameData)).thenReturn(true);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isTrue();

        when(gameQueryService.isMorbidMet(gameData)).thenReturn(false);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isFalse();
        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("ControlsAnotherPermanent intervening if: judged as the ability triggers, kept on the effect")
    void controlsAnotherGate() {
        PermanentPredicate filter = new PermanentIsCreaturePredicate();
        ConditionalEffect effect =
                new ConditionalEffect(new ControlsAnotherPermanent(filter), new DrawCardEffect(1));
        Permanent other = new Permanent(new Card());
        gameData.playerBattlefields.put(controllerId, List.of(other));

        // ConditionEvaluationService now evaluates the predicate through the FilterContext-aware
        // overload (permanent, filter, filterContext), building the context internally — match with any().
        when(predicateEvaluationService.matchesPermanentPredicate(eq(other), eq(filter), any())).thenReturn(true);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isTrue();

        when(predicateEvaluationService.matchesPermanentPredicate(eq(other), eq(filter), any())).thenReturn(false);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isFalse();
        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("Raid intervening if: judged as the ability triggers, kept on the effect to be checked again as it resolves")
    void raidGate() {
        ConditionalEffect effect = new ConditionalEffect(new Raid(), new DrawCardEffect(1));

        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isFalse();

        gameData.playersDeclaredAttackersThisTurn.add(controllerId);
        assertThat(resolver.ownEntryConditionMet(ctx(true, 0, false), effect.condition())).isTrue();
        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("Collect-evidence gate keeps the ETB effect only when evidence was paid")
    void collectEvidenceGate() {
        ConditionalEffect effect = new ConditionalEffect(
                new CollectEvidenceCostPaid(), new DrawCardEffect(1));
        Permanent source = new Permanent(card);
        EtbEffectContext sourceContext = new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, source);

        source.setCollectEvidenceCostPaid(true);
        assertThat(effect.asItTriggersOnEntering(facts(sourceContext))).isSameAs(effect.wrapped());

        source.setCollectEvidenceCostPaid(false);
        assertThat(effect.asItTriggersOnEntering(facts(sourceContext))).isEqualTo(new DrawCardEffect(0));
    }

    @Test
    @DisplayName("Sacrificed-card intervening if is judged as the ability triggers, with the card sacrificed to cast it")
    void sacrificedCardInterveningIf() {
        CardPredicate filter = new CardTypePredicate(CardType.CREATURE);
        ConditionalEffect effect = new ConditionalEffect(
                new SacrificedCardMatches(filter, "a creature"), new DrawCardEffect(1));
        Permanent source = new Permanent(card);
        EtbEffectContext sourceContext = new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, source);

        assertThat(resolver.ownEntryConditionMet(sourceContext, effect.condition())).isFalse();

        Card sacrificed = new Card();
        source.setCastSacrificedCard(sacrificed);
        when(predicateEvaluationService.matchesCardPredicate(eq(sacrificed), eq(filter), any(), eq(gameData), any()))
                .thenReturn(true);
        assertThat(resolver.ownEntryConditionMet(sourceContext, effect.condition())).isTrue();

        when(predicateEvaluationService.matchesCardPredicate(eq(sacrificed), eq(filter), any(), eq(gameData), any()))
                .thenReturn(false);
        assertThat(resolver.ownEntryConditionMet(sourceContext, effect.condition())).isFalse();
        assertThat(effect.asItTriggersOnEntering(facts(sourceContext))).isSameAs(effect);
    }

    @Test
    @DisplayName("An intervening if on the ability's target passes through, since no target is chosen yet")
    void targetConditionPassesThrough() {
        ConditionalEffect effect = new ConditionalEffect(
                new TargetPermanentMatches(new PermanentIsCreaturePredicate()), new DrawCardEffect(1));

        assertThat(effect.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isSameAs(effect);
    }

    @Test
    @DisplayName("Evoke's sacrifice is its controller's sacrifice when the evoke cost was paid, and doesn't trigger otherwise")
    void evokeSacrificeIsJudgedAsItTriggers() {
        SacrificeSelfIfEvokedEffect evokeSacrifice = new SacrificeSelfIfEvokedEffect();
        EtbEffectContext evoked = new EtbEffectContext(gameData, card, controllerId, true, 0, false, true, false);

        assertThat(evokeSacrifice.asItTriggersOnEntering(facts(evoked))).isEqualTo(new SacrificeSelfEffect(true));
        assertThat(evokeSacrifice.asItTriggersOnEntering(facts(ctx(true, 0, false)))).isNull();
    }

    @Test
    @DisplayName("\"If tribute wasn't paid\" is the wrapped effect when tribute wasn't paid, and doesn't trigger when it was")
    void tributeIsJudgedAsItTriggers() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        TributeNotPaidEffect tribute = new TributeNotPaidEffect(wrapped);
        Permanent source = new Permanent(card);
        EtbEffectContext sourceContext = new EtbEffectContext(gameData, card, controllerId,
                true, 0, false, false, false, source);

        assertThat(tribute.asItTriggersOnEntering(facts(sourceContext))).isSameAs(wrapped);

        source.setTributePaid(true);
        assertThat(tribute.asItTriggersOnEntering(facts(sourceContext))).isNull();
    }
}

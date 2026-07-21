package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.BeforeEach;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.mockito.Mock;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Unit tests for {@link EtbEffectResolver}: verifies each mandatory ETB effect is unwrapped,
 * materialised, or intervening-if gated exactly as the former {@code BattlefieldEntryService}
 * cascade did.
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
        resolver = new EtbEffectResolver(new ConditionEvaluationService(gameQueryService, predicateEvaluationService,
                new StaticEffectSupport(gameQueryService, predicateEvaluationService)));
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player1");
        card = new Card();
        card.setName("Test Creature");
    }

    private EtbEffectContext ctx(boolean wasCastFromHand, int etbMode, boolean kicked) {
        return new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode, kicked, false, false);
    }

    @Test
    @DisplayName("Unregistered effect passes through unchanged (default identity)")
    void defaultIdentity() {
        DrawCardEffect draw = new DrawCardEffect(1);
        assertThat(resolver.resolve(ctx(true, 0, false), draw)).isSameAs(draw);
    }

    @Test
    @DisplayName("LoseGameIfNotCastFromHand: dropped when cast from hand")
    void loseGameCastFromHandDropped() {
        assertThat(resolver.resolve(ctx(true, 0, false), new LoseGameIfNotCastFromHandEffect())).isNull();
    }

    @Test
    @DisplayName("LoseGameIfNotCastFromHand: controller loses when not cast from hand")
    void loseGameNotFromHand() {
        CardEffect resolved = resolver.resolve(ctx(false, 0, false), new LoseGameIfNotCastFromHandEffect());
        assertThat(resolved).isInstanceOf(TargetPlayerLosesGameEffect.class);
        assertThat(((TargetPlayerLosesGameEffect) resolved).playerId()).isEqualTo(controllerId);
    }

    @Test
    @DisplayName("ChooseOne: unwraps the option chosen at cast time")
    void chooseOneUnwrapsSelectedMode() {
        DrawCardEffect opt0 = new DrawCardEffect(1);
        GainLifeEffect opt1 = new GainLifeEffect(2);
        ChooseOneEffect modal = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", opt0, null),
                new ChooseOneEffect.ChooseOneOption("Gain", opt1, null)));

        assertThat(resolver.resolve(ctx(true, 1, false), modal)).isSameAs(opt1);
    }

    @Test
    @DisplayName("ChooseOne: out-of-range mode falls back to the first option")
    void chooseOneOutOfRangeFallsBackToFirst() {
        DrawCardEffect opt0 = new DrawCardEffect(1);
        ChooseOneEffect modal = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", opt0, null)));

        assertThat(resolver.resolve(ctx(true, 5, false), modal)).isSameAs(opt0);
    }

    @Test
    @DisplayName("ChooseOne: optional modal with etbMode -1 drops the trigger")
    void optionalChooseOneSkipsWhenNoModeChosen() {
        DrawCardEffect opt0 = new DrawCardEffect(1);
        ChooseOneEffect modal = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", opt0, null)), true);

        assertThat(resolver.resolve(ctx(true, -1, false), modal)).isNull();
    }

    @Test
    @DisplayName("KickedConditional: unwraps when kicked, dropped otherwise")
    void kickedConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect kicked = new ConditionalEffect(new Kicked(), wrapped);

        assertThat(resolver.resolve(ctx(true, 0, true), kicked)).isSameAs(wrapped);
        assertThat(resolver.resolve(ctx(true, 0, false), kicked)).isNull();
    }

    @Test
    @DisplayName("CastFromZoneConditional(HAND): unwraps only when cast from hand")
    void castFromHandConditional() {
        DrawCardEffect wrapped = new DrawCardEffect(1);
        ConditionalEffect fromHand = new ConditionalEffect(new CastFromZone(Zone.HAND), wrapped);

        assertThat(resolver.resolve(ctx(true, 0, false), fromHand)).isSameAs(wrapped);
        assertThat(resolver.resolve(ctx(false, 0, false), fromHand)).isNull();
    }

    @Test
    @DisplayName("GainLifeEqualToToughness: materialises toughness read at trigger time")
    void gainLifeEqualToToughness() {
        card.setToughness(4);
        CardEffect resolved = resolver.resolve(ctx(true, 0, false), new GainLifeEqualToToughnessEffect());
        assertThat(resolved).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) resolved).amount()).isEqualTo(new Fixed(4));
    }

    @Test
    @DisplayName("Metalcraft gate: keeps the conditional effect when met, drops it otherwise")
    void metalcraftGate() {
        ConditionalEffect effect = new ConditionalEffect(new Metalcraft(), new DrawCardEffect(1));

        when(gameQueryService.isMetalcraftMet(gameData, controllerId)).thenReturn(true);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isSameAs(effect);

        when(gameQueryService.isMetalcraftMet(gameData, controllerId)).thenReturn(false);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isNull();
    }

    @Test
    @DisplayName("Morbid gate: keeps the conditional effect when met, drops it otherwise")
    void morbidGate() {
        ConditionalEffect effect = new ConditionalEffect(new Morbid(), new DrawCardEffect(1));

        when(gameQueryService.isMorbidMet(gameData)).thenReturn(true);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isSameAs(effect);

        when(gameQueryService.isMorbidMet(gameData)).thenReturn(false);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isNull();
    }

    @Test
    @DisplayName("ControlsAnotherPermanent gate: keeps when met, drops otherwise")
    void controlsAnotherGate() {
        PermanentPredicate filter = new PermanentIsCreaturePredicate();
        ConditionalEffect effect =
                new ConditionalEffect(new ControlsAnotherPermanent(filter), new DrawCardEffect(1));
        Permanent other = new Permanent(new Card());
        gameData.playerBattlefields.put(controllerId, List.of(other));

        // ConditionEvaluationService now evaluates the predicate through the FilterContext-aware
        // overload (permanent, filter, filterContext), building the context internally — match with any().
        when(predicateEvaluationService.matchesPermanentPredicate(eq(other), eq(filter), any())).thenReturn(true);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isSameAs(effect);

        when(predicateEvaluationService.matchesPermanentPredicate(eq(other), eq(filter), any())).thenReturn(false);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isNull();
    }

    @Test
    @DisplayName("Raid gate: keeps the conditional effect when controller attacked, drops otherwise")
    void raidGate() {
        ConditionalEffect effect = new ConditionalEffect(new Raid(), new DrawCardEffect(1));

        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isNull();

        gameData.playersDeclaredAttackersThisTurn.add(controllerId);
        assertThat(resolver.resolve(ctx(true, 0, false), effect)).isSameAs(effect);
    }
}

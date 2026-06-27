package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaticEffectResolutionService {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

    @HandlesStaticEffect(value = ControllerLifeThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerLifeThresholdConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeThresholdConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 20);
        if (lifeTotal >= conditional.lifeThreshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }

    @HandlesStaticEffect(value = ControllerLifeAtOrBelowThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerLifeAtOrBelowThresholdConditionalSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect) effect;
        if (!isControllerLifeAtOrBelow(context, conditional.lifeThreshold())) return;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            if (boost.scope() == GrantScope.SELF || boost.scope() == GrantScope.ALL_OWN_CREATURES) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    @HandlesStaticEffect(ControllerLifeAtOrBelowThresholdConditionalEffect.class)
    private void resolveControllerLifeAtOrBelowThresholdConditionalOthers(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect) effect;
        if (!isControllerLifeAtOrBelow(context, conditional.lifeThreshold())) return;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            if (matchesCreatureScope(context, boost.scope(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    private boolean isControllerLifeAtOrBelow(StaticEffectContext context, int threshold) {
        return support.isControllerLifeAtOrBelow(context, threshold);
    }

    @HandlesStaticEffect(value = ControllerGraveyardCardThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerGraveyardCardThresholdConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerGraveyardCardThresholdConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, conditional.filter(), null)) {
                    count++;
                }
            }
        }
        if (count >= conditional.threshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }

    @HandlesStaticEffect(value = TopCardOfLibraryColorConditionalEffect.class, selfOnly = true)
    private void resolveTopCardColorConditionalSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        if (!isTopCardOfLibraryColor(context, conditional.color())) return;
        // Cards with "CARDNAME and other [type]" always buff themselves regardless of filter (CR 201.5).
        // The filter only applies to "other" creatures via the non-self handler.
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            accumulator.addKeywords(grant.keywords());
        }
    }

    @HandlesStaticEffect(TopCardOfLibraryColorConditionalEffect.class)
    private void resolveTopCardColorConditionalOthers(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            if (!isTopCardOfLibraryColor(context, conditional.color())) return;
            boolean scopeMatch = switch (boost.scope()) {
                case OWN_CREATURES, ALL_OWN_CREATURES -> context.targetOnSameBattlefield();
                case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                case ALL_CREATURES -> true;
                default -> false;
            };
            if (scopeMatch && matchesStaticFilter(context.target(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            if (!isTopCardOfLibraryColor(context, conditional.color())) return;
            if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    private boolean isTopCardOfLibraryColor(StaticEffectContext context, CardColor color) {
        return support.isTopCardOfLibraryColor(context, color);
    }

    private UUID findControllerId(GameData gameData, Permanent permanent) {
        return support.findControllerId(gameData, permanent);
    }

    private boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        return support.matchesCreatureScope(context, scope, filter);
    }

    private boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        return support.matchesStaticFilter(target, filter);
    }

}


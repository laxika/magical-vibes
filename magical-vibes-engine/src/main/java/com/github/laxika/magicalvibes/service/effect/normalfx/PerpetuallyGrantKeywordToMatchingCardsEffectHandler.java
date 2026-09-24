package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordToMatchingCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordToMatchingCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetual = (PerpetuallyGrantKeywordToMatchingCardsEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        UUID sourcePermanentId = entry.getSourcePermanentId();

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of());
        for (Permanent permanent : battlefield) {
            if (permanent.getId().equals(sourcePermanentId)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    permanent, perpetual.permanentFilter(), filterContext)) {
                continue;
            }
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, perpetual.keywords());
            if (grantableKeywords.isEmpty() || permanent.getOriginalCard() == null) {
                continue;
            }
            permanent.getPersistentGrantedKeywords().addAll(grantableKeywords);
            addPerpetualKeywords(gameData, permanent.getOriginalCard().getId(), grantableKeywords);
            addPerpetualTriggeredAbility(gameData, permanent.getOriginalCard().getId(), perpetual);
            addPersistentTriggeredAbility(permanent, perpetual);
        }

        for (Card card : gameData.playerHands.getOrDefault(entry.getControllerId(), List.of())) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, perpetual.handFilter(), null, gameData, entry.getControllerId())) {
                continue;
            }
            addPerpetualKeywords(gameData, card.getId(), perpetual.keywords());
            addPerpetualTriggeredAbility(gameData, card.getId(), perpetual);
        }
    }

    private Set<Keyword> grantableKeywords(GameData gameData, Permanent permanent, Set<Keyword> keywords) {
        return keywords.stream()
                .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword))
                .collect(java.util.stream.Collectors.toSet());
    }

    private void addPerpetualKeywords(GameData gameData, UUID cardId, Set<Keyword> keywords) {
        gameData.perpetualKeywords.merge(cardId, Set.copyOf(keywords), (existing, added) -> {
            Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
            merged.addAll(existing);
            merged.addAll(added);
            return Set.copyOf(merged);
        });
    }

    private void addPerpetualTriggeredAbility(GameData gameData, UUID cardId,
                                               PerpetuallyGrantKeywordToMatchingCardsEffect effect) {
        if (effect.triggeredAbilitySlot() == null) {
            return;
        }
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            List<CardEffect> effects = updated.computeIfAbsent(effect.triggeredAbilitySlot(),
                    ignoredSlot -> new ArrayList<>());
            if (!effects.contains(effect.triggeredAbility())) {
                effects.add(effect.triggeredAbility());
            }
            updated.replaceAll((slot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });
    }

    private void addPersistentTriggeredAbility(Permanent permanent,
                                                PerpetuallyGrantKeywordToMatchingCardsEffect effect) {
        if (effect.triggeredAbilitySlot() != null
                && !permanent.getPersistentTriggeredEffects(effect.triggeredAbilitySlot())
                        .contains(effect.triggeredAbility())) {
            permanent.addPersistentTriggeredEffect(effect.triggeredAbilitySlot(), effect.triggeredAbility());
        }
    }
}

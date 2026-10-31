package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
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
            if (permanent.getId().equals(sourcePermanentId) && !perpetual.keywords().contains(Keyword.TOXIC)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    permanent, perpetual.permanentFilter(), filterContext)) {
                continue;
            }
            Set<Keyword> grantableKeywords = grantableKeywords(gameData, permanent, perpetual.keywords());
            if (grantableKeywords.isEmpty() || permanent.getOriginalCard() == null) {
                continue;
            }
            restoreOrStackPerpetualKeywords(gameData, permanent, grantableKeywords);
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

        applyToCardsInZone(gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of()),
                perpetual.graveyardFilter(), gameData, entry, perpetual);
        applyToCardsInZone(gameData.playerDecks.getOrDefault(entry.getControllerId(), List.of()),
                perpetual.libraryFilter(), gameData, entry, perpetual);
    }

    private void applyToCardsInZone(List<Card> cards, CardPredicate filter,
                                    GameData gameData, StackEntry entry,
                                    PerpetuallyGrantKeywordToMatchingCardsEffect effect) {
        if (filter == null) {
            return;
        }
        for (Card card : cards) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, filter, null, gameData, entry.getControllerId())) {
                addPerpetualKeywords(gameData, card.getId(), effect.keywords());
                addPerpetualTriggeredAbility(gameData, card.getId(), effect);
            }
        }
    }

    /**
     * A grant newer than an earlier perpetual loss of the keyword restores it (the loss no longer applies);
     * a keyword the permanent still has gains another instance, which matters for keywords that trigger
     * separately.
     */
    private void restoreOrStackPerpetualKeywords(GameData gameData, Permanent permanent, Set<Keyword> keywords) {
        UUID cardId = permanent.getCard().getId();
        Set<Keyword> removed = gameData.perpetualCardRemovedKeywords.get(cardId);
        for (Keyword keyword : keywords) {
            boolean lost = removed != null && removed.remove(keyword);
            if (!lost && permanent.getPersistentGrantedKeywords().contains(keyword)) {
                gameData.perpetualCardExtraKeywordInstances
                        .computeIfAbsent(cardId, ignored -> new java.util.concurrent.ConcurrentHashMap<>())
                        .merge(keyword, 1, Integer::sum);
            }
        }
    }

    private Set<Keyword> grantableKeywords(GameData gameData, Permanent permanent, Set<Keyword> keywords) {
        return keywords.stream()
                .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword))
                .collect(java.util.stream.Collectors.toSet());
    }

    private void addPerpetualKeywords(GameData gameData, UUID cardId, Set<Keyword> keywords) {
        if (keywords.contains(Keyword.TOXIC)) {
            gameData.perpetualToxicValues.merge(cardId, 1, Integer::sum);
        }
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
        gameData.grantPerpetualAbility(cardId, effect.triggeredAbilitySlot(), effect.triggeredAbility());
    }

    private void addPersistentTriggeredAbility(Permanent permanent,
                                                PerpetuallyGrantKeywordToMatchingCardsEffect effect) {
        if (effect.triggeredAbilitySlot() != null) {
            permanent.addPersistentTriggeredEffect(effect.triggeredAbilitySlot(), effect.triggeredAbility());
        }
    }
}

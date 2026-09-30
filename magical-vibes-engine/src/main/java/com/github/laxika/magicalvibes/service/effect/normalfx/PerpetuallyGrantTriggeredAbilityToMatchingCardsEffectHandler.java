package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Records a triggered ability on each matching permanent and hand card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTriggeredAbilityToMatchingCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetual = (PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> battlefield = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of());
        for (Permanent permanent : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(
                    permanent, perpetual.permanentFilter(), filterContext)
                    || permanent.getOriginalCard() == null) {
                continue;
            }
            UUID cardId = permanent.getOriginalCard().getId();
            addPerpetualGrant(gameData, cardId, perpetual);
            if (!permanent.getPersistentTriggeredEffects(perpetual.triggeredAbilitySlot())
                    .contains(perpetual.triggeredAbility())) {
                permanent.addPersistentTriggeredEffect(
                        perpetual.triggeredAbilitySlot(), perpetual.triggeredAbility());
            }
        }

        for (Card card : gameData.playerHands.getOrDefault(entry.getControllerId(), List.of())) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, perpetual.handFilter(), null, gameData, entry.getControllerId())) {
                addPerpetualGrant(gameData, card.getId(), perpetual);
            }
        }
    }

    private void addPerpetualGrant(GameData gameData, UUID cardId,
                                   PerpetuallyGrantTriggeredAbilityToMatchingCardsEffect effect) {
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
}

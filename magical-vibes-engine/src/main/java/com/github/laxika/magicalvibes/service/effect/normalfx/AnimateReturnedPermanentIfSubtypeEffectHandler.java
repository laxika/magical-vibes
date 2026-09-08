package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnimateReturnedPermanentIfSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateReturnedPermanentIfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnimateReturnedPermanentIfSubtypeEffect animateEffect =
                (AnimateReturnedPermanentIfSubtypeEffect) effect;
        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            returnedCardId = entry.getTargetCardIds().getFirst();
        }
        if (returnedCardId == null) {
            return;
        }

        Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
        if (returnedPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                gameData, returnedPermanent, new PermanentHasSubtypePredicate(animateEffect.subtype()))) {
            return;
        }

        if (animateEffect.animation().duration() == EffectDuration.PERMANENT) {
            AmountContext amountContext = AmountContext.forStackEntry(entry, returnedPermanent);
            int power = animateEffect.animation().power() == null
                    ? printedPower(returnedPermanent)
                    : amountEvaluationService.evaluate(gameData, animateEffect.animation().power(), amountContext);
            int toughness = animateEffect.animation().toughness() == null
                    ? printedToughness(returnedPermanent)
                    : amountEvaluationService.evaluate(gameData, animateEffect.animation().toughness(), amountContext);
            animationSupport.animatePermanently(gameData, returnedPermanent, animateEffect.animation(),
                    power, toughness, entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId());
        } else {
            UUID originalTargetId = entry.getTargetId();
            entry.setTargetId(returnedPermanent.getId());
            try {
                animationSupport.animateSingle(gameData, entry, animateEffect.animation());
            } finally {
                entry.setTargetId(originalTargetId);
            }
        }
    }

    private int printedPower(Permanent permanent) {
        return permanent.getCard().getPower() == null ? 0 : permanent.getCard().getPower();
    }

    private int printedToughness(Permanent permanent) {
        return permanent.getCard().getToughness() == null ? 0 : permanent.getCard().getToughness();
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent;
                }
            }
        }
        return null;
    }
}

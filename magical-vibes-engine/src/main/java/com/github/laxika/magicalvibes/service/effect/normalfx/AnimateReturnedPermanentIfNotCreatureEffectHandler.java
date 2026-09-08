package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentIfNotCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnimateReturnedPermanentIfNotCreatureEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateReturnedPermanentIfNotCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnimateReturnedPermanentIfNotCreatureEffect animateEffect =
                (AnimateReturnedPermanentIfNotCreatureEffect) effect;
        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            returnedCardId = entry.getTargetCardIds().getFirst();
        }
        if (returnedCardId == null) {
            return;
        }

        Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
        if (returnedPermanent == null || !animateEffect.appliesTo(returnedPermanent.getCard())) {
            return;
        }

        AnimatePermanentsEffect animation = animateEffect.animation();
        AmountContext context = AmountContext.forStackEntry(entry, returnedPermanent);
        int power = animation.power() == null
                ? printedPower(returnedPermanent.getCard())
                : amountEvaluationService.evaluate(gameData, animation.power(), context);
        int toughness = animation.toughness() == null
                ? printedToughness(returnedPermanent.getCard())
                : amountEvaluationService.evaluate(gameData, animation.toughness(), context);
        animationSupport.animatePermanently(gameData, returnedPermanent, animation, power, toughness,
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId());
    }

    private int printedPower(Card card) {
        return card.getPower() == null ? 0 : card.getPower();
    }

    private int printedToughness(Card card) {
        return card.getToughness() == null ? 0 : card.getToughness();
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

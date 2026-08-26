package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnimateReturnedPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateReturnedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnimatePermanentsEffect animation =
                ((AnimateReturnedPermanentsEffect) effect).animation();
        for (UUID returnedCardId : entry.getTargetCardIds()) {
            Permanent returnedPermanent = findPermanentByCardId(gameData, returnedCardId);
            if (returnedPermanent == null) {
                continue;
            }

            AmountContext amountContext = AmountContext.forStackEntry(entry, returnedPermanent);
            int power = amountEvaluationService.evaluate(gameData, animation.power(), amountContext);
            int toughness = amountEvaluationService.evaluate(gameData, animation.toughness(), amountContext);
            animationSupport.animatePermanently(gameData, returnedPermanent, animation,
                    power, toughness, entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId());
        }
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                if (cardId.equals(card.getId())
                        || permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }
}

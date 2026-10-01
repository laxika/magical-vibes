package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTopCreatureCardInLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies a perpetual power/toughness boost to the first creature card in a library. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostTopCreatureCardInLibraryEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTopCreatureCardInLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostTopCreatureCardInLibraryEffect) effect;
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData, boost.powerBoost(),
                AmountContext.forStackEntry(entry, null));
        for (int i = 0; i < library.size(); i++) {
            Card card = library.get(i);
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            Card modifiedCard = card.createRuntimeCopy();
            modifiedCard.addEffect(EffectSlot.STATIC,
                    new StaticBoostEffect(amount, amount, GrantScope.SELF));
            library.set(i, modifiedCard);
            return;
        }
    }
}

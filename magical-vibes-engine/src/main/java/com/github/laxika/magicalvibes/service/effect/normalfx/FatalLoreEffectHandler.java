package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient;
import com.github.laxika.magicalvibes.model.effect.FatalLoreEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Fatal Lore on resolution: carries out the mode the opponent chose while the spell was cast (CR 700.2e,
 * see {@code MayPenaltyChoiceHandlerService#handleFatalLoreChoice}) as the next steps of this resolution.
 * Accepted is "you draw three cards"; declined is "you destroy up to two target creatures that player
 * controls, they can't be regenerated, that player draws up to three cards". The controller chose those
 * targets right after the opponent's choice, as the spell was cast (its ruling; CR 601.2c), so they were checked
 * again as it began to resolve (CR 608.2b) and only the legal ones are destroyed, together.
 */
@Component
public class FatalLoreEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FatalLoreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Boolean accepted = ((FatalLoreEffect) effect).accepted();
        if (accepted == null) {
            return;
        }
        List<CardEffect> chosenMode = accepted
                ? List.of(new DrawCardEffect(3))
                : List.of(new DestroyEachTargetPermanentEffect(true),
                        new DrawUpToNCardsEffect(3, DrawUpToRecipient.OPPONENT));
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                entry.insertEffectsToResolve(i + 1, chosenMode);
                return;
            }
        }
    }
}

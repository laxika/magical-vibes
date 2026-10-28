package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToNCreaturesOpponentControlsEffect;
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
 * controls, they can't be regenerated, that player draws up to three cards". The controller picks the
 * creatures on resolution rather than as targets while casting.
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
                : List.of(new DestroyUpToNCreaturesOpponentControlsEffect(2, true),
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

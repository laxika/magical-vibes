package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllLandsFromHandAndDiscardEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the hand-to-battlefield and discard sequence used by Manabond. */
@Component
@RequiredArgsConstructor
public class PutAllLandsFromHandAndDiscardEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final PutLandsFromHandSupport putLandsFromHandSupport;
    private final DiscardHandEffectHandler discardHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllLandsFromHandAndDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        cardRevealService.revealHandToAllPlayers(gameData, controllerId);
        putLandsFromHandSupport.applyPutChoice(
                gameData,
                controllerId,
                putLandsFromHandSupport.landCardIds(gameData, controllerId),
                entry.getCard().getName());
        discardHandEffectHandler.resolve(gameData, entry, new DiscardHandEffect());
    }
}

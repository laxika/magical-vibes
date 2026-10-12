package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.stereotype.Component;

@Component
public class ConjureCardInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    public ConjureCardInGraveyardEffectHandler(GraveyardService graveyardService,
                                               TriggerCollectionService triggerCollectionService) {
        this.graveyardService = graveyardService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardInGraveyardEffect conjure = (ConjureCardInGraveyardEffect) effect;
        Card card = conjure.cardFactory().get();
        card.setOwnerId(entry.getControllerId());
        card.freeze();
        graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), card);
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}

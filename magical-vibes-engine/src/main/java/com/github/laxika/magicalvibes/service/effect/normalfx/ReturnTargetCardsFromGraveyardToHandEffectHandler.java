package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnTargetCardsFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final MayPayManaEffectHandler mayPayManaEffectHandler;

    public ReturnTargetCardsFromGraveyardToHandEffectHandler(GraveyardReturnSupport graveyardReturnSupport) {
        this(graveyardReturnSupport, null);
    }

    @Autowired
    public ReturnTargetCardsFromGraveyardToHandEffectHandler(
            GraveyardReturnSupport graveyardReturnSupport,
            MayPayManaEffectHandler mayPayManaEffectHandler) {
        this.graveyardReturnSupport = graveyardReturnSupport;
        this.mayPayManaEffectHandler = mayPayManaEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardsFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCardsFromGraveyardToHandEffect) effect;
        if (e.unlessAnyPlayerPaysX()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            if (effectIndex >= 0 && mayPayManaEffectHandler != null) {
                String manaCost = "{" + entry.getXValue() + "}";
                entry.replaceEffectToResolve(effectIndex, new MayPayManaEffect(
                        manaCost,
                        null,
                        "Pay " + manaCost + " to prevent " + entry.getCard().getName() + "'s effect?",
                        MayPayPayer.ANY_PLAYER,
                        e.withoutAnyPlayerPaysX(),
                        0,
                        false));
                mayPayManaEffectHandler.resolve(gameData, entry, entry.getEffectsToResolve().get(effectIndex));
                return;
            }
        }

        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");
    }
}

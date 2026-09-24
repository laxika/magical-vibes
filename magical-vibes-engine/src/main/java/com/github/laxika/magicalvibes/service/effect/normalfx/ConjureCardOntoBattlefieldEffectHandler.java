package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a battlefield conjure for the ability's controller. */
@Component
@RequiredArgsConstructor
public class ConjureCardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardOntoBattlefieldEffect conjure = (ConjureCardOntoBattlefieldEffect) effect;
        Card card = conjure.cardFactory().get();
        card.setOwnerId(entry.getControllerId());
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, entry.getControllerId(), permanent, conjure.enterTappedTypes());
        entry.getCreatedPermanentIds().add(permanent.getId());
        if (card.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, entry.getControllerId(), card);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), card, null, false);
        }
    }
}

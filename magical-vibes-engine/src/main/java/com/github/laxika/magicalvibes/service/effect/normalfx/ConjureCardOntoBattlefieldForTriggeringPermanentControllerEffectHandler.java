package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a battlefield conjure for the controller of the permanent that caused the trigger. */
@Component
@RequiredArgsConstructor
public class ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect conjure =
                (ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect) effect;
        UUID controllerId = entry.getTriggeringPermanentControllerId();
        if (controllerId == null || !gameData.playerIds.contains(controllerId)) {
            return;
        }

        Card card = conjure.cardFactory().get();
        card.setOwnerId(controllerId);
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent, conjure.enterTappedTypes());
        if (card.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, card);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, card, null, false);
        }
    }
}

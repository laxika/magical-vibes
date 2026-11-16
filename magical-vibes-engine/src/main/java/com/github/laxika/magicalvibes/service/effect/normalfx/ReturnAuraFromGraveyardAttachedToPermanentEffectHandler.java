package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardAttachedToPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnAuraFromGraveyardAttachedToPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAuraFromGraveyardAttachedToPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnAuraFromGraveyardAttachedToPermanentEffect) effect;

        Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, e.enchantedPermanentId());
        if (enchantedPermanent == null || !gameQueryService.isCreature(gameData, enchantedPermanent)) {
            return;
        }

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, e.auraCardId());
        // An Aura that left the graveyard and came back is a new object (CR 400.7).
        if (auraCard == null || e.auraCardId().equals(entry.getTriggeringCardId())
                && !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(e.auraCardId()))) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, e.auraCardId());
        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(enchantedPermanent.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, e.returnControllerId(), auraPermanent);
        gameLogService.append(gameData, GameLog.builder()
                .card(auraCard)
                .text(" returns to the battlefield attached to ")
                .card(enchantedPermanent.getCard())
                .text(" (delayed trigger).")
                .build());
    }
}

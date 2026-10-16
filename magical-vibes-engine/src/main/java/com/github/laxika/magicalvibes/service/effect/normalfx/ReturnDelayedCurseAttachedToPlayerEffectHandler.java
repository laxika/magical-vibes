package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedReturnCurseAttachedToPlayer;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDelayedCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Lynde's delayed "return that Curse to the battlefield attached to you" trigger, which goes on
 * the stack as the end step begins. A Curse that has left and re-entered the graveyard is a new object and
 * is not returned (CR 400.7).
 */
@Component
@RequiredArgsConstructor
public class ReturnDelayedCurseAttachedToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDelayedCurseAttachedToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DelayedReturnCurseAttachedToPlayer pending = ((ReturnDelayedCurseAttachedToPlayerEffect) effect).pending();
        Card curseCard = gameQueryService.findCardInGraveyardById(gameData, pending.cardId());
        if (curseCard == null
                || gameData.graveyardEntryVersion(curseCard.getId()) != pending.graveyardEntryVersion()
                || !curseCard.isAura() || !curseCard.isEnchantPlayer()
                || !gameData.playerIds.contains(pending.attachedPlayerId())
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, curseCard, Zone.GRAVEYARD)
                || !auraAttachmentService.canEnchantPlayer(
                gameData, curseCard, pending.ownerId(), pending.attachedPlayerId())) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, pending.cardId());
        Permanent cursePermanent = new Permanent(curseCard);
        cursePermanent.setAttachedTo(pending.attachedPlayerId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, pending.ownerId(), cursePermanent);
        gameLogService.append(gameData, GameLog.builder()
                .card(curseCard)
                .text(" returns to the battlefield attached to ")
                .text(gameData.playerIdToName.get(pending.attachedPlayerId()))
                .text(" (delayed trigger).")
                .build());
    }
}

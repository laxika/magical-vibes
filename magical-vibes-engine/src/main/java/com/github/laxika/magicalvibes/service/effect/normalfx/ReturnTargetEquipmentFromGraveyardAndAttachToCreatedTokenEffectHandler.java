package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetCardIdsForEffect(effect);
        if (targetIds.isEmpty()) {
            return;
        }

        Card card = gameQueryService.findCardInGraveyardById(gameData, targetIds.getFirst());
        if (card == null || !card.getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return;
        }

        Permanent equipment = graveyardReturnSupport.reanimateTargetedCard(
                gameData, entry.getControllerId(), card);
        if (equipment == null) {
            return;
        }

        Permanent token = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (token == null || !equipSupport.canAttachEquipment(gameData, equipment, token)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(token.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, token.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", token.getCard(), "."));
        log.info("Game {} - {} attached to created token {} via {}", gameData.id,
                equipment.getCard().getName(), token.getCard().getName(), entry.getCard().getName());
    }
}

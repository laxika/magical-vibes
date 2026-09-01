package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceFromGraveyardAttachedToEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceCardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, sourceCardId);
        if (sourceCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (card is no longer in the graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, sourceCardId);
        Permanent equipment = new Permanent(sourceCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), equipment);

        Permanent enteringCreature = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (enteringCreature != null && equipSupport.canAttachEquipment(gameData, equipment, enteringCreature)) {
            equipSupport.attachEquipment(gameData, equipment, enteringCreature);
            gameLogService.append(gameData, GameLog.builder()
                    .card(sourceCard)
                    .text(" returns to the battlefield attached to ")
                    .card(enteringCreature.getCard())
                    .text(".")
                    .build());
            log.info("Game {} - {} returns attached to {}", gameData.id,
                    sourceCard.getName(), enteringCreature.getCard().getName());
        } else {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " returns to the battlefield unattached."));
            log.info("Game {} - {} returns to the battlefield unattached", gameData.id, sourceCard.getName());
        }
    }
}

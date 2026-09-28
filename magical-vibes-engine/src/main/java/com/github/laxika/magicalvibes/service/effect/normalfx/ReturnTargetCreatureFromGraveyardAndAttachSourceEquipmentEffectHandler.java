package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardAndAttachSourceEquipmentEffect;
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
public class ReturnTargetCreatureFromGraveyardAndAttachSourceEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureFromGraveyardAndAttachSourceEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnTargetCreatureFromGraveyardAndAttachSourceEquipmentEffect) effect;
        UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        UUID controllerId = entry.getControllerId();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = targetCardId == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, targetCardId);

        if (targetCard == null
                || !controllerId.equals(graveyardOwnerId)
                || !gameQueryService.matchesCardPredicate(targetCard, returnEffect.filter(), entry.getCard().getId())) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (target creature is no longer in the graveyard)."));
            return;
        }
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, targetCard, Zone.GRAVEYARD)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
        Permanent creature = new Permanent(targetCard);
        creature.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, creature);

        Permanent equipment = entry.getSourcePermanentId() == null
                ? equipSupport.findEquipmentByCardId(gameData, entry.getCard().getId())
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment != null && equipSupport.attachEquipment(gameData, equipment, creature)) {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(entry.getCard(), " is now attached to ", targetCard, "."));
        }

        gameLogService.append(gameData,
                GameLog.textCardText(gameData.playerIdToName.get(controllerId) + " returns ", targetCard,
                        " to the battlefield."));
        log.info("Game {} - {} returns {} and attaches {} to it", gameData.id,
                entry.getCard().getName(), targetCard.getName(), entry.getCard().getName());
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, creature, targetCard);
    }
}

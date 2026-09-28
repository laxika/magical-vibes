package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndAurasOrEquipmentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureAndAurasOrEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureAndAurasOrEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent creature = returnCreature(gameData, entry.targetsForGroup(0), controllerId);
        for (UUID attachmentCardId : entry.targetsForGroup(1)) {
            returnAttachment(gameData, attachmentCardId, controllerId, creature);
        }
    }

    private Permanent returnCreature(GameData gameData, List<UUID> targetIds, UUID controllerId) {
        if (targetIds.isEmpty()) {
            return null;
        }
        Card creatureCard = findGraveyardCard(gameData, controllerId, targetIds.getFirst());
        if (creatureCard == null || !creatureCard.hasType(CardType.CREATURE)) {
            return null;
        }
        return graveyardReturnSupport.reanimateTargetedCard(gameData, controllerId, creatureCard);
    }

    private void returnAttachment(GameData gameData, UUID cardId, UUID controllerId, Permanent creature) {
        Card card = findGraveyardCard(gameData, controllerId, cardId);
        if (card == null || (!card.isAura() && !card.getSubtypes().contains(CardSubtype.EQUIPMENT))) {
            return;
        }

        boolean isEquipment = card.getSubtypes().contains(CardSubtype.EQUIPMENT);
        Permanent attachment = new Permanent(card);
        boolean attach = creature != null
                && (isEquipment
                ? equipSupport.canAttachEquipment(gameData, attachment, creature)
                : auraAttachmentService.canEnchant(gameData, card, controllerId, creature));
        if (!isEquipment && !attach) {
            return;
        }
        if (attach) {
            attachment.setAttachedTo(creature.getId());
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, attachment);
        if (isEquipment && attach && gameQueryService.findPermanentById(gameData, attachment.getId()) != null) {
            equipSupport.notifyEquipmentAttached(gameData, attachment, null);
        }

        GameLog.Builder log = GameLog.builder().card(card);
        if (attach) {
            log.text(" enters the battlefield attached to ").card(creature.getCard()).text(".");
        } else {
            log.text(" enters the battlefield unattached.");
        }
        gameLogService.append(gameData, log.build());
    }

    private Card findGraveyardCard(GameData gameData, UUID controllerId, UUID cardId) {
        return gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndAurasOrEquipmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureAndAurasOrEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final EquipSupport equipSupport;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureAndAurasOrEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        // "Return target creature card ..., then return up to two target Aura and/or Equipment cards": two events, in
        // that order (CR 608.2c), the attachments entering together.
        List<Permanent> creature = new ArrayList<>(1);
        gameData.rulesEvents.separately(() -> {
            Permanent returned = returnCreature(gameData, entry.targetsForGroup(0), controllerId);
            if (returned != null) creature.add(returned);
        });
        Permanent host = creature.isEmpty() ? null : creature.getFirst();
        List<BattlefieldEntryCard> attachments = new ArrayList<>();
        for (UUID attachmentCardId : entry.targetsForGroup(1)) {
            BattlefieldEntryCard attachment = attachment(gameData, attachmentCardId, controllerId, host);
            if (attachment != null) attachments.add(attachment);
        }
        if (!attachments.isEmpty()) {
            gameData.rulesEvents.separately(() -> battlefieldEntryBatchSupport.begin(gameData, attachments));
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

    /**
     * An Aura enters attached to that creature or, if it can't enchant it, stays in the graveyard (the card's reminder
     * text, CR 303.4g); an Equipment that can't be attached to it enters unattached (the card's ruling).
     */
    private BattlefieldEntryCard attachment(GameData gameData, UUID cardId, UUID controllerId, Permanent creature) {
        Card card = findGraveyardCard(gameData, controllerId, cardId);
        if (card == null) {
            return null;
        }
        if (card.isAura()) {
            return new BattlefieldEntryCard(controllerId, controllerId, card, Zone.GRAVEYARD, null)
                    .withHosts(creature == null ? List.of() : List.of(creature.getId()));
        }
        if (!card.getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return null;
        }
        boolean attach = creature != null
                && gameQueryService.findPermanentById(gameData, creature.getId()) != null
                && equipSupport.canAttachEquipment(gameData, new Permanent(card), creature);
        return new BattlefieldEntryCard(controllerId, controllerId, card, Zone.GRAVEYARD,
                attach ? creature.getId() : null);
    }

    private Card findGraveyardCard(GameData gameData, UUID controllerId, UUID cardId) {
        return gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}

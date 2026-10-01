package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsAttachedToOpposingCreaturesEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Conjures one full card copy attached to each opposing creature. */
@Component
@RequiredArgsConstructor
public class ConjureCardsAttachedToOpposingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final ConjureCardToBattlefieldEffectHandler conjureCardToBattlefieldEffectHandler;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsAttachedToOpposingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardsAttachedToOpposingCreaturesEffect conjure =
                (ConjureCardsAttachedToOpposingCreaturesEffect) effect;
        for (Permanent creature : opposingCreatures(gameData, entry.getControllerId())) {
            int createdBefore = entry.getCreatedPermanentIds().size();
            conjureCardToBattlefieldEffectHandler.resolve(
                    gameData, entry, new ConjureCardToBattlefieldEffect(conjure.cardName()));
            if (entry.getCreatedPermanentIds().size() == createdBefore) {
                continue;
            }

            UUID createdId = entry.getCreatedPermanentIds().get(createdBefore);
            Permanent aura = gameQueryService.findPermanentById(gameData, createdId);
            if (aura == null || !auraAttachmentService.canEnchant(
                    gameData, aura.getCard(), entry.getControllerId(), creature)) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
            aura.setAttachedTo(creature.getId());
            aura.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, creature.getId());
        }
    }

    private List<Permanent> opposingCreatures(GameData gameData, UUID controllerId) {
        List<Permanent> creatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatures.add(permanent);
                }
            }
        }
        return creatures;
    }
}

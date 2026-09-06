package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToReturnedPermanentEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenAttachedToReturnedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToReturnedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAttachedToReturnedPermanentEffect) effect;
        UUID returnedCardId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getCard() == null ? null : entry.getCard().getId();
        Permanent returned = findPermanentByCardId(gameData, returnedCardId);
        UUID returnedControllerId = returned == null
                ? null : gameQueryService.findPermanentController(gameData, returned.getId());
        boolean targetControllerMatches = switch (e.targetControllerRelation()) {
            case SELF -> entry.getControllerId().equals(returnedControllerId);
            case OPPONENT -> returnedControllerId != null && !entry.getControllerId().equals(returnedControllerId);
            case ANY -> true;
        };
        if (returned == null || !gameQueryService.isCreature(gameData, returned) || !targetControllerMatches) {
            return;
        }

        AmountContext context = AmountContext.forStackEntry(entry, returned);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);

        Card preview = TokenCardFactory.create(e.token(), power, toughness, entry.getCard().getSetCode());
        Permanent previewPermanent = new Permanent(preview);
        if (!auraAttachmentService.canEnchant(gameData, preview, entry.getControllerId(), returned)
                || gameQueryService.hasProtectionFromSource(gameData, returned, previewPermanent)) {
            return;
        }

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), amount,
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        for (UUID createdId : createdIds) {
            Permanent role = gameQueryService.findPermanentById(gameData, createdId);
            if (role == null || !auraAttachmentService.canEnchant(
                    gameData, role.getCard(), entry.getControllerId(), returned)) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(role.getId());
            role.setAttachedTo(returned.getId());
            role.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkAuraAttachedTriggers(gameData, role, returned.getId());
        }
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }
}

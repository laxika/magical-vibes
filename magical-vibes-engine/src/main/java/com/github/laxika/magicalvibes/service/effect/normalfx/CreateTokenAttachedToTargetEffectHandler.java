package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
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
public class CreateTokenAttachedToTargetEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAttachedToTargetEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        UUID targetControllerId = target == null
                ? null : gameQueryService.findPermanentController(gameData, target.getId());
        boolean targetControllerMatches = switch (e.targetControllerRelation()) {
            case SELF -> entry.getControllerId().equals(targetControllerId);
            case OPPONENT -> targetControllerId != null && !entry.getControllerId().equals(targetControllerId);
            case ANY -> true;
        };
        if (target == null || !gameQueryService.isCreature(gameData, target) || !targetControllerMatches) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);

        Card preview = TokenCardFactory.create(e.token(), power, toughness, entry.getCard().getSetCode());
        Permanent previewPermanent = new Permanent(preview);
        if (!auraAttachmentService.canEnchant(gameData, preview, entry.getControllerId(), target)
                || gameQueryService.hasProtectionFromSource(gameData, target, previewPermanent)) {
            return;
        }

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), amount,
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        for (UUID createdId : createdIds) {
            Permanent role = gameQueryService.findPermanentById(gameData, createdId);
            if (role == null || !auraAttachmentService.canEnchant(
                    gameData, role.getCard(), entry.getControllerId(), target)) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(role.getId());
            role.setAttachedTo(target.getId());
            role.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkAuraAttachedTriggers(gameData, role, target.getId());
        }
    }
}

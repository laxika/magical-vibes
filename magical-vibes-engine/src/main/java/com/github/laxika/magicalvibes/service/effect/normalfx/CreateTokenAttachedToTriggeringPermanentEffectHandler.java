package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTriggeringPermanentEffect;
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
public class CreateTokenAttachedToTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createEffect = (CreateTokenAttachedToTriggeringPermanentEffect) effect;
        UUID triggeringPermanentId = entry.getTriggeringPermanentId();
        Permanent target = triggeringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, triggeringPermanentId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        AmountContext context = AmountContext.forStackEntry(entry, target);
        int amount = amountEvaluationService.evaluate(gameData, createEffect.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, createEffect.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, createEffect.token().toughness(), context);

        Card preview = TokenCardFactory.create(createEffect.token(), power, toughness, entry.getCard().getSetCode());
        Permanent previewPermanent = new Permanent(preview);
        if (!auraAttachmentService.canEnchant(gameData, preview, entry.getControllerId(), target)
                || gameQueryService.hasProtectionFromSource(gameData, target, previewPermanent)) {
            return;
        }

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), createEffect.token(), amount,
                entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        for (UUID createdId : createdIds) {
            Permanent aura = gameQueryService.findPermanentById(gameData, createdId);
            if (aura == null || !auraAttachmentService.canEnchant(
                    gameData, aura.getCard(), entry.getControllerId(), target)) {
                continue;
            }
            gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
            aura.setAttachedTo(target.getId());
            aura.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, target.getId());
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CreateTokenWithTriggeredAbilitiesOfExiledCardsEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    public CreateTokenWithTriggeredAbilitiesOfExiledCardsEffectHandler(
            PermanentControlSupport permanentControlSupport,
            GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService) {
        this.permanentControlSupport = permanentControlSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect tokenEffect =
                (CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, tokenEffect.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, tokenEffect.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, tokenEffect.token().toughness(), context);

        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                tokenEffect.token(),
                amount,
                entry.getCard().getSetCode(),
                power,
                toughness,
                triggeredAbilitiesOfExiledCards(gameData, entry.getSourcePermanentId())
        ));
    }

    private Map<EffectSlot, List<EffectRegistration>> triggeredAbilitiesOfExiledCards(
            GameData gameData, UUID sourcePermanentId) {
        Map<EffectSlot, List<EffectRegistration>> result = new EnumMap<>(EffectSlot.class);
        if (sourcePermanentId == null) {
            return result;
        }
        for (Card card : gameData.getCardsExiledByPermanent(sourcePermanentId)) {
            for (EffectSlot slot : EffectSlot.values()) {
                if (!isTriggeredAbilitySlot(slot)) {
                    continue;
                }
                List<EffectRegistration> registrations = card.getEffectRegistrations(slot);
                if (!registrations.isEmpty()) {
                    result.computeIfAbsent(slot, ignored -> new java.util.ArrayList<>()).addAll(registrations);
                }
            }
        }
        return result;
    }

    private boolean isTriggeredAbilitySlot(EffectSlot slot) {
        return switch (slot) {
            case ON_TAP, SPELL, STATIC, MAY_SKIP_DRAW_STEP_DRAW, MULLIGAN_ACTION -> false;
            default -> true;
        };
    }
}

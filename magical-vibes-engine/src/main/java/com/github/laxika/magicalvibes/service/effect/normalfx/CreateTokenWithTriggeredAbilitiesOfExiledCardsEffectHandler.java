package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
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

    private List<Ability> triggeredAbilitiesOfExiledCards(GameData gameData, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return List.of();
        }
        Map<EffectSlot, List<EffectRegistration>> slotted = new EnumMap<>(EffectSlot.class);
        List<Ability> abilities = new ArrayList<>();
        for (Card card : gameData.getCardsExiledByPermanent(sourcePermanentId)) {
            for (EffectSlot slot : EffectSlot.values()) {
                List<EffectRegistration> registrations = card.getEffectRegistrations(slot);
                if (!registrations.isEmpty()) {
                    slotted.computeIfAbsent(slot, ignored -> new ArrayList<>()).addAll(registrations);
                }
            }
            abilities.addAll(card.getUnslottedTriggeredAbilities());
        }
        List<Ability> result = new ArrayList<>(LegacySlots.registrationAbilities(slotted));
        result.addAll(abilities);
        return result;
    }
}

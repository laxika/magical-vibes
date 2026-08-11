package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AwardAnyColorManaEffect} on the stack (Manamorphose), mirroring the mana-ability
 * path in {@code ActivatedAbilityExecutionService}. The chosen-subtype restrictions read their type
 * off a source permanent, so they add nothing here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardAnyColorManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardAnyColorManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardAnyColorManaEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        boolean prompted = AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                entry.getControllerId(), e, amount, false, source == null ? null : source.getChosenSubtype(),
                source == null ? null : source.getCard(), source == null ? null : source.getId());
        if (prompted) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            log.info("Game {} - Awaiting {} to choose a mana color ({})", gameData.id, playerName, e.restriction());
        }
    }
}

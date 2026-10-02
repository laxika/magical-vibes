package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        }

        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = targetCardId == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || !controllerId.equals(graveyardOwnerId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in your graveyard)."));
            return;
        }

        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .targetGraveyard(true)
                .build();
        graveyardReturnSupport.resolvePreTargetedById(
                gameData, entry, returnEffect, controllerId, entry.getCard().getId(), targetCardId);

        if (gameQueryService.cardHasType(targetCard, CardType.CREATURE, gameData, controllerId)) {
            gameData.perpetualCardPowerToughnessModifiers.merge(
                    targetCard.getId(),
                    new CardPowerToughnessModifier(e.powerBoost(), e.toughnessBoost()),
                    (oldValue, newValue) -> oldValue.add(newValue.power(), newValue.toughness()));
            gameLogService.append(gameData, GameLog.cardThen(targetCard,
                    " perpetually gets " + formatModifier(e.powerBoost(), e.toughnessBoost()) + "."));
        }
    }

    private static String formatModifier(int power, int toughness) {
        return String.format("%+d/%+d", power, toughness);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardByExperienceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardFromGraveyardByExperienceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardFromGraveyardByExperienceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnTargetCardFromGraveyardByExperienceEffect experienceEffect =
                (ReturnTargetCardFromGraveyardByExperienceEffect) effect;
        UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            return;
        }

        int experience = gameData.playerExperienceCounters.getOrDefault(entry.getControllerId(), 0);
        GraveyardChoiceDestination destination = targetCard.getManaValue() <= experience
                ? GraveyardChoiceDestination.BATTLEFIELD
                : GraveyardChoiceDestination.HAND;
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(destination)
                .filter(experienceEffect.filter())
                .targetGraveyard(true)
                .build();
        graveyardReturnSupport.resolvePreTargetedById(gameData, entry, returnEffect,
                entry.getControllerId(), entry.getCard().getId(), targetCardId);
    }
}

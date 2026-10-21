package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Time Sidewalk's pregame choice to exile itself from the opening hand. */
@Component
@RequiredArgsConstructor
public class ExileSourceCardFromOpeningHandMayHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect effect = ability.effects().stream()
                .filter(ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect.class::isInstance)
                .map(ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect.class::cast)
                .findFirst().orElseThrow();
        mayMiscHandlerService.handleOpeningHandExileChoice(gameData, player, accepted, ability, effect);
    }
}

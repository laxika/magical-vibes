package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInExileOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles The Master's optional copy replacement choice. */
@Component
@RequiredArgsConstructor
public class CopyCreatureCardInExileOnEnterChoiceHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCreatureCardInExileOnEnterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CopyCreatureCardInExileOnEnterEffect effect = ability.effects().stream()
                .filter(CopyCreatureCardInExileOnEnterEffect.class::isInstance)
                .map(CopyCreatureCardInExileOnEnterEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayCopyHandlerService.handleCopyCreatureCardInExileOnEnterChoice(
                gameData, player, accepted, ability, effect);
    }
}

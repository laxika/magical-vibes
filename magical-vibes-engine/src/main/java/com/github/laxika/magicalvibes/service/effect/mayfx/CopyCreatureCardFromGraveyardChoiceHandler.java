package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CopyCreatureCardFromGraveyardChoiceHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCreatureCardFromGraveyardOnEnterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CopyCreatureCardFromGraveyardOnEnterEffect effect = ability.effects().stream()
                .filter(CopyCreatureCardFromGraveyardOnEnterEffect.class::isInstance)
                .map(CopyCreatureCardFromGraveyardOnEnterEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayCopyHandlerService.handleCopyCreatureCardFromGraveyardChoice(
                gameData, player, accepted, ability, effect);
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyLandFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CopyLandFromGraveyardChoiceHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyLandFromGraveyardOnEnterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CopyLandFromGraveyardOnEnterEffect effect = ability.effects().stream()
                .filter(e -> e instanceof CopyLandFromGraveyardOnEnterEffect)
                .map(e -> (CopyLandFromGraveyardOnEnterEffect) e)
                .findFirst().orElse(null);
        mayCopyHandlerService.handleCopyLandFromGraveyardChoice(gameData, player, accepted, ability, effect);
    }
}

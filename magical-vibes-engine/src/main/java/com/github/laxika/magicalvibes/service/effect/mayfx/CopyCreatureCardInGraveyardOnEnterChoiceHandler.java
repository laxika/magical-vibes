package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Superior Spider-Man's graveyard copy replacement choice. */
@Component
@RequiredArgsConstructor
public class CopyCreatureCardInGraveyardOnEnterChoiceHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCreatureCardInGraveyardOnEnterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CopyCreatureCardInGraveyardOnEnterEffect effect = ability.effects().stream()
                .filter(CopyCreatureCardInGraveyardOnEnterEffect.class::isInstance)
                .map(CopyCreatureCardInGraveyardOnEnterEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayCopyHandlerService.handleCopyCreatureCardInGraveyardOnEnterChoice(
                gameData, player, accepted, ability, effect);
    }
}

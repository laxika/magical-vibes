package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Play-from-graveyard-without-paying — e.g. Horde of Notions.
 */
@Component
@RequiredArgsConstructor
public class PlayTargetCardFromGraveyardWithoutPayingHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayTargetCardFromGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PlayTargetCardFromGraveyardWithoutPayingManaCostEffect playFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof PlayTargetCardFromGraveyardWithoutPayingManaCostEffect)
                .map(e -> (PlayTargetCardFromGraveyardWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (playFromGraveyardEffect != null) {
            mayCastHandlerService.handlePlayFromGraveyardChoice(gameData, player, accepted, ability, playFromGraveyardEffect);
        }
    }
}

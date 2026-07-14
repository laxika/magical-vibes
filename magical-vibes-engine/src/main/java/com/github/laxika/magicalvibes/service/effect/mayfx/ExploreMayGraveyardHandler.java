package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Explore — may put revealed non-land card into graveyard.
 */
@Component
@RequiredArgsConstructor
public class ExploreMayGraveyardHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayMiscHandlerService.handleExploreMayGraveyardChoice(gameData, player, accepted);
    }
}

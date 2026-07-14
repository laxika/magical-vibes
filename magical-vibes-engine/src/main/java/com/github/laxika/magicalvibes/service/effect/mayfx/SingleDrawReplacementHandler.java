package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generic single-draw replacement — you may replace your next draw with an alternative effect.
 */
@Component
@RequiredArgsConstructor
public class SingleDrawReplacementHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReplaceSingleDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReplaceSingleDrawEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ReplaceSingleDrawEffect)
                .map(e -> (ReplaceSingleDrawEffect) e)
                .findFirst().orElse(null);
        mayMiscHandlerService.handleSingleDrawReplacementChoice(gameData, player, accepted, ability, effect);
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MimeoplasmCopyOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles The Mimeoplasm's optional two-card graveyard copy replacement. */
@Component
@RequiredArgsConstructor
public class MimeoplasmCopyOnEnterChoiceHandler implements MayEffectHandlerBean {

    private final MayCopyHandlerService mayCopyHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MimeoplasmCopyOnEnterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayCopyHandlerService.handleMimeoplasmCopyOnEnterChoice(gameData, player, accepted, ability);
    }
}

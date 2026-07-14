package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ParadigmMayCastFromExileEffect;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * "You may cast the exiled card" (Paradigm Shift / suspend-style exile casts).
 */
@Component
public class ParadigmMayCastFromExileHandler implements MayEffectHandlerBean {

    // @Lazy breaks the circular dependency:
    // ParadigmService → PlayerInputService → MayAbilityChoiceInteractionHandler → MayAbilityHandlerService
    private final ParadigmService paradigmService;

    public ParadigmMayCastFromExileHandler(@Lazy ParadigmService paradigmService) {
        this.paradigmService = paradigmService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ParadigmMayCastFromExileEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        paradigmService.handleMayCastChoice(gameData, player, accepted, ability);
    }
}

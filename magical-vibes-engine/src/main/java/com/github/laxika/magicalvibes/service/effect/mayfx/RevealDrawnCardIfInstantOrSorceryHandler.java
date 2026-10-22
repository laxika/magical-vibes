package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardIfInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealDrawnCardIfInstantOrSorceryEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the "you may reveal the card you draw as you draw it" choice, then resumes the draw. */
@Component
@RequiredArgsConstructor
public class RevealDrawnCardIfInstantOrSorceryHandler implements MayEffectHandlerBean {

    private final RevealDrawnCardIfInstantOrSorceryEffectHandler revealHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardIfInstantOrSorceryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            RevealDrawnCardIfInstantOrSorceryEffect effect = ability.effects().stream()
                    .filter(RevealDrawnCardIfInstantOrSorceryEffect.class::isInstance)
                    .map(RevealDrawnCardIfInstantOrSorceryEffect.class::cast)
                    .findFirst().orElseThrow();
            revealHandler.reveal(gameData, ability.sourceCard(), player.getId(), ability.sourcePermanentId(), effect);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}

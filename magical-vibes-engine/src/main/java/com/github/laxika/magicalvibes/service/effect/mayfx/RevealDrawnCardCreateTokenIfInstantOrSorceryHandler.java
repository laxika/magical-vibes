package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardCreateTokenIfInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealDrawnCardCreateTokenIfInstantOrSorceryEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the "you may reveal the card you draw as you draw it" choice, then resumes the draw. */
@Component
@RequiredArgsConstructor
public class RevealDrawnCardCreateTokenIfInstantOrSorceryHandler implements MayEffectHandlerBean {

    private final RevealDrawnCardCreateTokenIfInstantOrSorceryEffectHandler revealHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardCreateTokenIfInstantOrSorceryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            RevealDrawnCardCreateTokenIfInstantOrSorceryEffect effect = ability.effects().stream()
                    .filter(RevealDrawnCardCreateTokenIfInstantOrSorceryEffect.class::isInstance)
                    .map(RevealDrawnCardCreateTokenIfInstantOrSorceryEffect.class::cast)
                    .findFirst().orElseThrow();
            revealHandler.reveal(gameData, ability.sourceCard(), player.getId(), ability.sourcePermanentId(), effect);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}

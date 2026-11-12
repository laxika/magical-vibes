package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.BecomeCopyOfCardUntilEndOfTurnEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeCopyOfCardUntilEndOfTurnChoiceHandler implements MayEffectHandlerBean {

    private final BecomeCopyOfCardUntilEndOfTurnEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            BecomeCopyOfCardUntilEndOfTurnEffect effect = ability.effects().stream()
                    .filter(BecomeCopyOfCardUntilEndOfTurnEffect.class::isInstance)
                    .map(BecomeCopyOfCardUntilEndOfTurnEffect.class::cast)
                    .findFirst()
                    .orElseThrow();
            // "That card" is the exiled card the question names, as it is in exile now.
            ExiledCardEntry exiled = ability.targetCardId() == null
                    ? null : gameData.findExiledCard(ability.targetCardId());
            if (exiled != null) {
                effectHandler.becomeCopy(gameData, ability.sourceCard(), ability.controllerId(),
                        ability.sourcePermanentId(), exiled.card(), effect.additionalKeywords());
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

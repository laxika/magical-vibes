package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.BecomeCopyOfCardUntilEndOfTurnEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
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
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    null,
                    ability.sourcePermanentId());
            effectHandler.resolve(gameData, entry, effect);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

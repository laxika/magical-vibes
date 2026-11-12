package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTextBoxesEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Answers "As [this creature] enters, you may exchange its text box and another creature's" (Deadpool, Trading
 * Card): a replacement effect of the entry (CR 614.1c), asked off the stack once the other creature is chosen. An
 * accepted exchange happens at once; either way the creature's entry then goes on to its enters abilities.
 */
@Component
@RequiredArgsConstructor
public class ExchangeTextBoxesAsEntersHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Autowired
    @Lazy
    private BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeTextBoxesEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Permanent entering = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (entering != null) {
            if (accepted) {
                StackEntry exchange = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(), ability.controllerId(), ability.description(),
                        ability.effects(), ability.targetCardId(), entering.getId());
                exchange.setNonTargeting(true);
                effectResolutionService.resolveEffects(gameData, exchange);
            }
            battlefieldEntryService.processCreatureETBEffects(gameData, ability.controllerId(),
                    entering.getCard(), null, entering.isCast(), 0, false);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

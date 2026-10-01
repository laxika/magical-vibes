package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCopySpellEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.TemptingOfferCopySpellEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Tempt with Mayhem. */
@Component
@RequiredArgsConstructor
public class TemptingOfferCopySpellHandler implements MayEffectHandlerBean {

    private final TemptingOfferCopySpellEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCopySpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TemptingOfferCopySpellEffect effect =
                (TemptingOfferCopySpellEffect) ability.effects().getFirst();
        effectHandler.completeChoice(gameData, ability, effect, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

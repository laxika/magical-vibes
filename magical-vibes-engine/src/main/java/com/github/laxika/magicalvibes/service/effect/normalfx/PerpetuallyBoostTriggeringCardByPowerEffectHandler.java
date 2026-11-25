package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardByPowerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies a perpetual +X/+X modification using the triggering spell's power, to that spell's card while the spell is
 * still on the stack (a spell that left it is a new object, CR 400.7).
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostTriggeringCardByPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTriggeringCardByPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (var spell : subjectResolutionService.locatedCards(gameData, entry, effect,
                Subjects.cardsOf(Subjects.triggeringSpell()))) {
            // The card's current power, including characteristic-defining power while it is still a spell
            // (Maro) and any perpetual modifiers it already carries.
            Integer currentPower = gameQueryService.getEffectiveCardPower(gameData, spell.card());
            int power = Math.max(0, currentPower == null ? 0 : currentPower);
            PerpetualCardPowerToughnessSupport.remember(gameData, spell.card(), power, power);
        }
    }
}

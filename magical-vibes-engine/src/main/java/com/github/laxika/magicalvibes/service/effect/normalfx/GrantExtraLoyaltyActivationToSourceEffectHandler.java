package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an extra loyalty activation this turn for the source planeswalker only, a floating effect on it. */
@Component
@RequiredArgsConstructor
public class GrantExtraLoyaltyActivationToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantExtraLoyaltyActivationToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.getCard().hasType(CardType.PLANESWALKER)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, source.getId()))) {
            return;
        }
        // "You may activate one of its loyalty abilities once this turn as though none had been": an effect on the
        // planeswalker that lasts the turn (CR 514.2).
        gameData.addEffectOnPermanent(entry, effect, source.getId(), Duration.UNTIL_END_OF_TURN);
    }
}

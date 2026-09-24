package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDamageOpponentsByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Fateful Tempest's past-vote mill and damage branch. */
@Component
@RequiredArgsConstructor
public class MillControllerAndDamageOpponentsByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndDamageOpponentsByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerAndDamageOpponentsByManaValueEffect millEffect =
                (MillControllerAndDamageOpponentsByManaValueEffect) effect;
        int count = Math.max(0, millEffect.count());
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
        int totalManaValue = milled.stream().mapToInt(Card::getManaValue).sum();
        if (totalManaValue <= 0) {
            return;
        }

        dealDamageToPlayersEffectHandler.resolve(gameData, entry,
                new DealDamageToPlayersEffect(totalManaValue, DamageRecipient.EACH_OPPONENT));
        gameOutcomeService.checkWinCondition(gameData);
    }
}

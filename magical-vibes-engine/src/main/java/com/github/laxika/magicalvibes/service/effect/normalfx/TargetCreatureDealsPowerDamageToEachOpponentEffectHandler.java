package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null) {
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (sourceControllerId == null) {
            return;
        }

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                entry.getControllerId(),
                source.getCard().getName() + "'s ability",
                List.of(),
                null,
                source.getId());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
    }
}

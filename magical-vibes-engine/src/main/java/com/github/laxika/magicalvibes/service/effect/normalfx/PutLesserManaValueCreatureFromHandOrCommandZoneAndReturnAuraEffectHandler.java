package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffectHandler
        implements NormalEffectHandlerBean {

    private final PutCardFromHandOrGraveyardOntoBattlefieldSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect nextOfKinEffect =
                (PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect) effect;
        Integer dyingManaValue = nextOfKinEffect.dyingCreatureManaValue();
        if (dyingManaValue == null || dyingManaValue <= 0) {
            return;
        }

        UUID auraOwnerId = entry.getCard().getOwnerId() == null
                ? entry.getControllerId()
                : entry.getCard().getOwnerId();
        support.beginChoice(gameData, entry.getControllerId(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(dyingManaValue - 1))),
                "creature", entry.getCard().getId(), entry.getCard().getName(),
                null, 0, false, false,
                false, true, entry.getCard().getId(), auraOwnerId, true);
    }
}

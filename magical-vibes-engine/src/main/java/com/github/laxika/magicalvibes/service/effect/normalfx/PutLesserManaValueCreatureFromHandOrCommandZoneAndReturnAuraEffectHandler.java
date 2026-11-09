package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffectHandler
        implements NormalEffectHandlerBean {

    private final PutCardFromHandOrGraveyardOntoBattlefieldSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Lesser mana value" than the enchanted creature: the entry's triggering permanent, as it last existed on the
        // battlefield (CR 608.2h).
        int dyingManaValue = amountEvaluationService.evaluate(gameData, new TriggeringPermanentManaValue(),
                AmountContext.forStackEntry(entry, null));
        if (dyingManaValue <= 0) {
            return;
        }

        UUID auraReturnControllerId = entry.getControllerId();
        support.beginChoice(gameData, entry.getControllerId(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(dyingManaValue - 1))),
                "creature", entry.getCard().getId(), entry.getCard().getName(),
                null, 0, false, false,
                false, true, entry.getCard().getId(), auraReturnControllerId, true);
    }
}

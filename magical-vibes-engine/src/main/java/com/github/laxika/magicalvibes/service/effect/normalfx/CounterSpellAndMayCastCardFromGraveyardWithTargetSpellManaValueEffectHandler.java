package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect counterEffect =
                (CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect) effect;

        StackEntry targetEntry = counterSupport.findCounterTargetIgnoringCounterability(
                gameData, entry.getTargetId(), entry);
        if (targetEntry != null) {
            var targetCard = targetEntry.getTargetingCard();
            int xSymbols = targetCard.getManaCost() == null ? 0
                    : new ManaCost(targetCard.getManaCost()).getXSymbolCount();
            int targetSpellManaValue = targetCard.getManaValue() + xSymbols * targetEntry.getXValue();
            StackEntry counterableTarget = counterSupport.findCounterTarget(gameData, entry.getTargetId(), entry);
            if (counterableTarget != null) {
                counterSupport.counterSpell(gameData, entry, counterableTarget);
            }

            entry.setXValue(targetSpellManaValue);
            entry.setTargetId(null);
        }

        if (targetEntry == null && entry.getTargetId() == null) {
            return;
        }

        CardEffect castEffect = new CastCardFromGraveyardEffect(
                new CardAllOfPredicate(List.of(
                        counterEffect.cardFilter(),
                        new CardMaxManaValueXPredicate())),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                new CardAnyOfPredicate(List.of()),
                true,
                true);
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        entry.insertEffectsToResolve(effectIndex + 1, List.of(castEffect));
    }
}

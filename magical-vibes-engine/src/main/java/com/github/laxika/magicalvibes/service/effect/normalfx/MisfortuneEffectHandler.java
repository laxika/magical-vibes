package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MisfortuneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Misfortune on resolution: carries out the mode the opponent chose while the spell was cast (CR 700.2e,
 * see {@code MayPenaltyChoiceHandlerService#handleMisfortuneChoice}) as the next steps of this resolution.
 * Accepted puts a +1/+1 counter on each creature the controller controls and gains them 4 life; declined
 * puts a -1/-1 counter on each creature the opponent controls and deals 4 damage to them.
 */
@Component
@Slf4j
public class MisfortuneEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MisfortuneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Boolean accepted = ((MisfortuneEffect) effect).accepted();
        if (accepted == null) {
            return;
        }
        List<CardEffect> chosenMode = accepted
                ? List.of(
                        new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentIsCreaturePredicate()),
                        new GainLifeEffect(4))
                : List.of(
                        new PutCounterOnEachMatchingPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1,
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                                EachPermanentScope.ALL_PLAYERS),
                        new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT));
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                entry.insertEffectsToResolve(i + 1, chosenMode);
                return;
            }
        }
    }
}

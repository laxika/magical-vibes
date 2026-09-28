package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.NoteMostPrevalentCreatureTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "66")
public class FavoredEnemy extends Card {

    public FavoredEnemy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new NoteMostPrevalentCreatureTypeOnEnterEffect());

        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new FightTargetsEffect());

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                        new TriggeringCardConditionalEffect(
                                new CardHasSourceChosenSubtypePredicate(),
                                new PutCounterOnTargetPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE)));
    }
}

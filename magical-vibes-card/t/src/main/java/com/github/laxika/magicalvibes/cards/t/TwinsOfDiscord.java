package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfChosenManaValueParityCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "37")
public class TwinsOfDiscord extends Card {

    public TwinsOfDiscord() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, SequenceEffect.of(
                new ChooseManaValueParityAtResolutionEffect(),
                new CreaturesOfChosenManaValueParityCantBlockThisTurnEffect()));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new OpponentDealtDamageThisTurn(1),
                        new ControlledPermanentsEnterWithAdditionalCountersEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsColorlessPredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                                2)));
    }
}

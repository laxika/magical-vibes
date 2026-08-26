package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "246")
public class IdyllicGrange extends Card {

    public IdyllicGrange() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(2,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PLAINS))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new SourceUntapped(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}

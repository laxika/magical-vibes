package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "1")
public class CollectorsCage extends Card {

    public CollectorsCage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(5, true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(new Coven(), new PlayImprintedCardWithoutPayingManaCostEffect())
                ),
                "{1}, {T}: Put a +1/+1 counter on target creature you control. Then if you control three or more "
                        + "creatures with different powers, you may play the exiled card without paying its mana cost.",
                TargetFilters.creatureYouControl()
        ));
    }
}

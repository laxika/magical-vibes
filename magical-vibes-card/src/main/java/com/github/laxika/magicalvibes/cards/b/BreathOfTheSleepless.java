package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "11")
@CardRegistration(set = "VOC", collectorNumber = "49")
public class BreathOfTheSleepless extends Card {

    public BreathOfTheSleepless() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.SPIRIT)));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                                true));
    }
}

package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "28")
public class XhoCaiFlickeringTalon extends Card {

    public XhoCaiFlickeringTalon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReduceCastCostForNextMatchingSpellEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1));

        ControlledPermanentPredicateTargetFilter targetFilter = TargetFilters.creatureYouControl();
        target(targetFilter, 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                        2,
                        null,
                        List.of(FlickerEffect.flickerTarget()),
                        targetFilter
                ));
    }
}

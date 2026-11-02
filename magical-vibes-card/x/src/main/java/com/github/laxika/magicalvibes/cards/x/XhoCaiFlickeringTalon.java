package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "28")
public class XhoCaiFlickeringTalon extends Card {

    public XhoCaiFlickeringTalon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReduceCastCostForNextMatchingSpellEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), 1));

        ControlledPermanentPredicateTargetFilter targetFilter = TargetFilters.creatureYouControl();
        target(targetFilter, 0, 1)
                .addTrigger(Triggers.youCastSpell(), SpellCastTriggerEffect.nth(
                        2,
                        null,
                        List.of(FlickerEffect.flickerTarget()),
                        targetFilter
                ));
    }
}

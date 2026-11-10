package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "244")
@CardRegistration(set = "M3C", collectorNumber = "293")
@CardRegistration(set = "FDC", collectorNumber = "258")
public class ForsakenMonument extends Card {

    public ForsakenMonument() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsColorlessPredicate()));
        addTrigger(Triggers.youTapLandForMana(),
                new AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS));
        addTrigger(Triggers.youTapNonlandPermanentForMana(),
                new AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS));
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsColorlessPredicate()), new GainLifeEffect(2));
    }
}

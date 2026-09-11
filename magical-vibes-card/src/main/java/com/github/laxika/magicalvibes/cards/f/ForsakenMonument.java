package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "244")
public class ForsakenMonument extends Card {

    public ForsakenMonument() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentIsColorlessPredicate()));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS));
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA,
                new AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsColorlessPredicate(), List.of(new GainLifeEffect(2))));
    }
}

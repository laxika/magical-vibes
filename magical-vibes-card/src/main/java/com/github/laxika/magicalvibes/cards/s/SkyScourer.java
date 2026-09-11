package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "78")
public class SkyScourer extends Card {

    public SkyScourer() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsColorlessPredicate(),
                List.of(new BoostSelfEffect(1, 0))
        ));
    }
}

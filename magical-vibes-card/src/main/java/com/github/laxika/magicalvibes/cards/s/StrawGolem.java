package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "158")
public class StrawGolem extends Card {

    public StrawGolem() {
        // When an opponent casts a creature spell, sacrifice this creature.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new SacrificeSelfEffect())
        ));
    }
}

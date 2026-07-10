package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "206")
public class ElvishHandservant extends Card {

    public ElvishHandservant() {
        // Whenever a player casts a Giant spell, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.GIANT),
                        List.of(new PutCountersOnSourceEffect(1, 1, 1))
                ),
                "Put a +1/+1 counter on Elvish Handservant?"
        ));
    }
}

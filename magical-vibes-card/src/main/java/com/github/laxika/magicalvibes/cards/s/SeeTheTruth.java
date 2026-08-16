package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "M21", collectorNumber = "69")
public class SeeTheTruth extends Card {

    public SeeTheTruth() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.HAND),
                new LookAtTopCardsHandTopBottomEffect(3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastNotFromHand(),
                new LookAtTopCardsEffect(new Fixed(3), new Fixed(3), null,
                        LookDestination.BOTTOM_OF_LIBRARY, false)));
    }
}

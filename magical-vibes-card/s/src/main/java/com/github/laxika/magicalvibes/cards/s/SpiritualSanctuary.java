package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "38")
public class SpiritualSanctuary extends Card {

    public SpiritualSanctuary() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                new GainLifeEffect(new Fixed(1), GainLifeRecipient.TRIGGERING_PLAYER)));
    }
}

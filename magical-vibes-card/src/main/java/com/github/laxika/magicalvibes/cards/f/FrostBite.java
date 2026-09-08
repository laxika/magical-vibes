package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "KHM", collectorNumber = "138")
public class FrostBite extends Card {

    public FrostBite() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlsPermanentCount(3, new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(2),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(3)
        ));
    }
}

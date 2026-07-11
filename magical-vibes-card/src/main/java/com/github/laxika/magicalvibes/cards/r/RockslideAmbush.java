package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "PTK", collectorNumber = "121")
public class RockslideAmbush extends Card {

    public RockslideAmbush() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER)));
    }
}

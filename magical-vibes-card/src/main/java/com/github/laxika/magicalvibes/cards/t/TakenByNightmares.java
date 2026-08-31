package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "112")
public class TakenByNightmares extends Card {

    public TakenByNightmares() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                new ScryEffect(2)));
    }
}

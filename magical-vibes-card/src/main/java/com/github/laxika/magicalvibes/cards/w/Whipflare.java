package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NPH", collectorNumber = "102")
public class Whipflare extends Card {

    public Whipflare() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, false, false, new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}

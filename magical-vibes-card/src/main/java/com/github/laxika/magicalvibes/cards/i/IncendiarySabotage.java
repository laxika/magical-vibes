package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "KLD", collectorNumber = "119")
public class IncendiarySabotage extends Card {

    public IncendiarySabotage() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
    }
}

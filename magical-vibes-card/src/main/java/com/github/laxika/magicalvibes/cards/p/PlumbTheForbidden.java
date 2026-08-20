package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForXValueEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "STX", collectorNumber = "81")
public class PlumbTheForbidden extends Card {

    public PlumbTheForbidden() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForXValueEffect());
        addEffect(EffectSlot.SPELL,
                new SacrificeAnyNumberOfPermanentsCost(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1));
    }
}

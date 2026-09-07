package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "TOR", collectorNumber = "51")
public class CabalRitual extends Card {

    public CabalRitual() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(threshold),
                new AwardManaEffect(ManaColor.BLACK, 3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(threshold,
                new AwardManaEffect(ManaColor.BLACK, 5)));
    }
}

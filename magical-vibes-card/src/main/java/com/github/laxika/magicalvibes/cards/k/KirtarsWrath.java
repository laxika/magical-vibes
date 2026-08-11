package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ODY", collectorNumber = "28")
public class KirtarsWrath extends Card {

    public KirtarsWrath() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(7, null),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true),
                SequenceEffect.of(
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true),
                        CreateTokenEffect.whiteSpirit(2))));
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "161")
public class GolemsHeart extends Card {

    public GolemsHeart() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new MayEffect(new GainLifeOnSpellCastEffect(new CardTypePredicate(CardType.ARTIFACT), 1), "Gain 1 life?"));
    }
}

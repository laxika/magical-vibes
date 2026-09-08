package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CSP", collectorNumber = "61")
public class HaakonStromgaldScourge extends Card {

    public HaakonStromgaldScourge() {
        addCastingOption(GraveyardCast.castOnlyFromGraveyard());
        addEffect(EffectSlot.STATIC, new CastSpellsFromGraveyardEffect(
                new CardSubtypePredicate(CardSubtype.KNIGHT)));
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(2));
    }
}

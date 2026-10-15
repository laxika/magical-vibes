package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "44")
public class DoubleDown extends Card {

    public DoubleDown() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                CopyControllerCastSpellOnSpellCastEffect.tokenCopy(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ASSASSIN),
                                new CardSubtypePredicate(CardSubtype.MERCENARY),
                                new CardSubtypePredicate(CardSubtype.PIRATE),
                                new CardSubtypePredicate(CardSubtype.ROGUE),
                                new CardSubtypePredicate(CardSubtype.WARLOCK)
                        )), null, null));
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "31")
public class CloudhoofKirin extends Card {

    public CloudhoofKirin() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new MillTargetPlayerByCastSpellManaValueEffect(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)))),
                "Have target player mill cards equal to that spell's mana value?"));
    }
}

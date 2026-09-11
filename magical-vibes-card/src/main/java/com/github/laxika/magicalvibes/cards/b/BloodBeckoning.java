package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "92")
public class BloodBeckoning extends Card {

    public BloodBeckoning() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        targetWhenKicked(
                new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                1, 1, 2, 2)
                .addEffect(EffectSlot.SPELL,
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                creature, 2, null, false, false, 1, false, Set.of(), false));
    }
}

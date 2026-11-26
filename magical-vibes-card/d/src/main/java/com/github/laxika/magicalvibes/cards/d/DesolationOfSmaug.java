package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "93")
public class DesolationOfSmaug extends Card {

    public DesolationOfSmaug() {
        addSpellEffect(DealDamageEffect.toEachCreature(3,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))));
        addSpellEffect(new AwardAnyColorManaEffect(
                4, ManaSpendRestriction.SUBTYPE_SPELL, Set.of(CardSubtype.DRAGON), true));
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "97")
@CardRegistration(set = "AKR", collectorNumber = "160")
public class HourOfDevastation extends Card {

    public HourOfDevastation() {
        // All creatures lose indestructible until end of turn.
        addSpellEffect(new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.allCreatures()));

        // Deals 5 damage to each creature and each non-Bolas planeswalker.
        addSpellEffect(DealDamageEffect.toEachCreatureAndPlaneswalker(5, new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.BOLAS))))));
    }
}

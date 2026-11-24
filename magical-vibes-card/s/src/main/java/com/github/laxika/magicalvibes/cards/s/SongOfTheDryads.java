package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2027")
@CardRegistration(set = "CMM", collectorNumber = "324")
@CardRegistration(set = "CMM", collectorNumber = "572")
@CardRegistration(set = "C14", collectorNumber = "47")
@CardRegistration(set = "CMA", collectorNumber = "147")
public class SongOfTheDryads extends Card {

    public SongOfTheDryads() {
        target(TargetFilters.permanent())
                .addStaticEffect(new SetCardTypesEffect(Set.of(CardType.LAND), Subjects.attached()))
                .addStaticEffect(new EnchantedPermanentBecomesTypeEffect(CardSubtype.FOREST))
                .addStaticEffect(new BecomeColorlessEffect(Subjects.attached()));
    }
}

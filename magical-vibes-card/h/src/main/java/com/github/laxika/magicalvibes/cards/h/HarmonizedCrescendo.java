package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;

@CardRegistration(set = "ECL", collectorNumber = "54")
@CardRegistration(set = "ECL", collectorNumber = "357")
@CardRegistration(set = "ECL", collectorNumber = "384")
@CardRegistration(set = "ECL", collectorNumber = "394")
@CardRegistration(set = "ECL", collectorNumber = "408")
public class HarmonizedCrescendo extends Card {

    public HarmonizedCrescendo() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new DrawCardEffect(new PermanentCount(new PermanentHasTypeChosenThisResolutionPredicate(), CountScope.CONTROLLER)));
    }
}

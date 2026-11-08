package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "35")
public class FacesOfThePast extends Card {

    public FacesOfThePast() {
        // Whenever a creature dies, tap all untapped creatures that share a creature type with it or untap all
        // tapped creatures that share a creature type with it (chosen as the ability resolves).
        var sharesTypeWithIt = new PermanentSharesCreatureTypeWithTriggeringPermanentPredicate();
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Tap all untapped creatures that share a creature type with it",
                                new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, sharesTypeWithIt)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Untap all tapped creatures that share a creature type with it",
                                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, sharesTypeWithIt))))));
    }
}

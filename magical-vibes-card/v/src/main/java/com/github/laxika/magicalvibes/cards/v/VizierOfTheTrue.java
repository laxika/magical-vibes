package com.github.laxika.magicalvibes.cards.v;



import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "28")

public class VizierOfTheTrue extends Card {

    public VizierOfTheTrue() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SkipNextUntapEffect.exert(),
                "Exert Vizier of the True as it attacks?"));

        PermanentPredicateTargetFilter creatureAnOpponentControls = TargetFilters.creatureAnOpponentControls();
        target(creatureAnOpponentControls).addTrigger(Triggers.youExertCreature(),
                new TapPermanentsEffect(TapUntapScope.TARGET, creatureAnOpponentControls.predicate()));
    }
}

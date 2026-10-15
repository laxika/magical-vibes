package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOU", collectorNumber = "28")
public class VizierOfTheTrue extends Card {

    public VizierOfTheTrue() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Vizier of the True as it attacks?"));

        PermanentPredicateTargetFilter creatureAnOpponentControls = TargetFilters.creatureAnOpponentControls();
        target(creatureAnOpponentControls).addEffect(EffectSlot.ON_CONTROLLER_EXERTS,
                new TapPermanentsEffect(TapUntapScope.TARGET, creatureAnOpponentControls.predicate()));
    }
}

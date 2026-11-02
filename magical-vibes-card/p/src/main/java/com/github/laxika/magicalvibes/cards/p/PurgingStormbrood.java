package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AbsorbEssence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "213")
public class PurgingStormbrood extends Card {

    public PurgingStormbrood() {
        setBackFaceCard(new AbsorbEssence());
        addCastingOption(new OmenCast());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(0, 2));

        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new RemoveAllCountersFromTargetPermanentEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AbsorbEssence";
    }
}

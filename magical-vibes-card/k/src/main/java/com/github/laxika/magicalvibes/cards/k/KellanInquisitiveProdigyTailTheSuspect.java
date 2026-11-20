package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TailTheSuspect;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "212")
@CardRegistration(set = "MKM", collectorNumber = "334")
public class KellanInquisitiveProdigyTailTheSuspect extends Card {

    public KellanInquisitiveProdigyTailTheSuspect() {
        setBackFaceCard(new TailTheSuspect());
        addCastingOption(new AdventureCast("{G}{U}"));

        target(TargetFilters.artifact(), 0, 1).addTrigger(Triggers.attacks(),
                SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new ConditionalEffect(new TargetPermanentMatches(new PermanentControlledBySourceControllerPredicate()), new DrawCardEffect(1))));
    }

    @Override
    public String getBackFaceClassName() {
        return "TailTheSuspect";
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TailTheSuspect;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "212")
@CardRegistration(set = "MKM", collectorNumber = "334")
public class KellanInquisitiveProdigyTailTheSuspect extends Card {

    public KellanInquisitiveProdigyTailTheSuspect() {
        setBackFaceCard(new TailTheSuspect());
        addCastingOption(new AdventureCast("{G}{U}"));

        target(TargetFilters.artifact(), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new DrawCardEffect(1),
                        ThenEffectRecipient.CONTROLLER,
                        new PermanentControlledBySourceControllerPredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TailTheSuspect";
    }
}

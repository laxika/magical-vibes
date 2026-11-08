package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DistinctCounterKindsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "32")
@CardRegistration(set = "NCC", collectorNumber = "133")
public class StormOfForms extends Card {

    public StormOfForms() {
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(ReturnToHandEffect.target());
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new DistinctCounterKindsAmongControlledPermanents()));
    }
}

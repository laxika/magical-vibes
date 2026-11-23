package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "70")
public class TheWesternCloud extends Card {

    public TheWesternCloud() {
        addStaticEffect(new PreventAllDamageToCreaturesYouControlEffect(null));
        addStaticEffect(PreventDamageToRecipientsEffect.to(
                Subjects.controlledBy(Subjects.you(), new PermanentIsPlaneswalkerPredicate())));

        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                CreateTokenEffect.ofTappedTreasureToken(3),
                new MassDamageEffect(3, false, false, true, null)));
    }
}

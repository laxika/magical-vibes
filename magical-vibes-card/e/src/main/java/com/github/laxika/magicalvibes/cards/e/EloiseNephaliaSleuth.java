package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "3")
@CardRegistration(set = "MIC", collectorNumber = "41")
public class EloiseNephaliaSleuth extends Card {

    public EloiseNephaliaSleuth() {
        // Whenever another creature you control dies, investigate.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), CreateTokenEffect.ofClueToken(1));

        // Whenever you sacrifice a token, surveil 1.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsTokenPredicate(),
                        new SurveilEffect(1)));
    }
}

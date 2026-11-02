package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WildGooseChase;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "19")
public class AlbiorixGooseTyrant extends Card {

    public AlbiorixGooseTyrant() {
        setBackFaceCard(new WildGooseChase());
        addCastingOption(new AdventureCast("{G}{U}"));

        PerpetuallyBoostCardEffect sacrificeBoost = new PerpetuallyBoostCardEffect(this, 1, 1);
        addTrigger(Triggers.sacrificed(new PermanentIsTokenPredicate(), ObjectRelation.YOU_CONTROL), sacrificeBoost);
        addTrigger(new TriggerCondition(Triggers.sacrificed(Triggers.TOKEN, ObjectRelation.YOU_CONTROL),
                AbilityZones.EXILE), TriggerOptions.DEFAULT, sacrificeBoost);
    }

    @Override
    public String getBackFaceClassName() {
        return "WildGooseChase";
    }
}

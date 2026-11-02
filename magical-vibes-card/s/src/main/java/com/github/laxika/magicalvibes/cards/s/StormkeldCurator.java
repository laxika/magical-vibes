package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GiantSecrets;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "26")
public class StormkeldCurator extends Card {

    public StormkeldCurator() {
        setBackFaceCard(new GiantSecrets());
        addCastingOption(new AdventureCast("{X}{U}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new AttachAurasToSourceEffect(false, false, Integer.MAX_VALUE, false));
    }

    @Override
    public String getBackFaceClassName() {
        return "GiantSecrets";
    }
}

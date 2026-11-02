package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TakeItBack;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "237")
public class SpellscornCoven extends Card {

    public SpellscornCoven() {
        setBackFaceCard(new TakeItBack());
        addCastingOption(new AdventureCast("{2}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }

    @Override
    public String getBackFaceClassName() {
        return "TakeItBack";
    }
}

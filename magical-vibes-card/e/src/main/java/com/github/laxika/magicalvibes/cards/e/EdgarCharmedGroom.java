package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "236")
@CardRegistration(set = "DBL", collectorNumber = "503")
public class EdgarCharmedGroom extends Card {

    public EdgarCharmedGroom() {
        setBackFaceCard(new EdgarMarkovsCoffin());
        addStaticEffect(new StaticBoostEffect(
                1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceTransformedFromGraveyardEffect(false, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EdgarMarkovsCoffin";
    }
}

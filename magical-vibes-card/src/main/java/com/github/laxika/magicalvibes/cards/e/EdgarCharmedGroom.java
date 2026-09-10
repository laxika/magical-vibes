package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "236")
public class EdgarCharmedGroom extends Card {

    public EdgarCharmedGroom() {
        setBackFaceCard(new EdgarMarkovsCoffin());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect(false, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EdgarMarkovsCoffin";
    }
}

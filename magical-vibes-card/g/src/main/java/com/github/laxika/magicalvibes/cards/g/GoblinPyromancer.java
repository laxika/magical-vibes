package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "206")
public class GoblinPyromancer extends Card {

    public GoblinPyromancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostAllCreaturesEffect(3, 0, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureDamagedByMatchingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "84")
@CardRegistration(set = "SLD", collectorNumber = "635")
public class ToxinSliver extends Card {

    public ToxinSliver() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DestroyCreatureDamagedByMatchingCreatureEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER), true, true));
    }
}

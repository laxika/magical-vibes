package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "43")
public class VampireSlayer extends Card {

    public VampireSlayer() {
        // Whenever this creature deals damage to a Vampire, destroy that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new DestroyDamagedCreatureEffect(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
    }
}

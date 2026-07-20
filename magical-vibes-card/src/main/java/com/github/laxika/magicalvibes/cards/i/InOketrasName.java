package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "AKH", collectorNumber = "19")
public class InOketrasName extends Card {

    public InOketrasName() {
        // Zombies you control get +2/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                2, 1, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)
        ));
        // Other creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                1, 1, new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))
        ));
    }
}

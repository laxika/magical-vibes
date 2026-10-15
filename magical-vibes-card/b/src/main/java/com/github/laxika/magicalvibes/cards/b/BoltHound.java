package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M21", collectorNumber = "131")
public class BoltHound extends Card {

    public BoltHound() {
        // Whenever this creature attacks, other creatures you control get +1/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1, 0, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }
}

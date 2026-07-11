package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "PTK", collectorNumber = "95")
public class ZhangHeWeiGeneral extends Card {

    public ZhangHeWeiGeneral() {
        // Horsemanship is loaded from Scryfall metadata.
        // Whenever Zhang He attacks, each other creature you control gets +1/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1, 0, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ROE", collectorNumber = "9")
public class PathrazerOfUlamog extends Card {

    public PathrazerOfUlamog() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                3, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}

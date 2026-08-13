package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "USG", collectorNumber = "179")
public class CraterHellion extends Card {

    public CraterHellion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MassDamageEffect(4, false, false,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{4}{R}{R}"));
    }
}

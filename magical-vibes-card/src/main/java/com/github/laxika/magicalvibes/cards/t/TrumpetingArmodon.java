package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "262")
public class TrumpetingArmodon extends Card {

    public TrumpetingArmodon() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new MustBlockSourceEffect(null)), "{1}{G}: Target creature blocks Trumpeting Armodon this turn if able."));
    }
}

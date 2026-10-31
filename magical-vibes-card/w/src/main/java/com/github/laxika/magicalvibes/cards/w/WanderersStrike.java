package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "38")
@CardRegistration(set = "CMM", collectorNumber = "69")
public class WanderersStrike extends Card {

    public WanderersStrike() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new ProliferateEffect());
    }
}

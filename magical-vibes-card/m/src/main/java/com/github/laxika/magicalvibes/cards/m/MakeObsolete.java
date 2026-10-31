package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KLD", collectorNumber = "89")
@CardRegistration(set = "KLR", collectorNumber = "97")
public class MakeObsolete extends Card {

    public MakeObsolete() {
        addSpellEffect(new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}

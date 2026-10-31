package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TDM", collectorNumber = "38")
public class BewilderingBlizzard extends Card {

    public BewilderingBlizzard() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new BoostAllCreaturesEffect(-3, 0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}

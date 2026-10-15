package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DSK", collectorNumber = "253")
public class MarvinMurderousMimic extends Card {

    public MarvinMurderousMimic() {
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfOwnCreaturesEffect(
                new PermanentNotPredicate(new PermanentHasSameNameAsSourcePredicate())));
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "65")
public class RankAndFile extends Card {

    public RankAndFile() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllCreaturesEffect(-1, -1,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}

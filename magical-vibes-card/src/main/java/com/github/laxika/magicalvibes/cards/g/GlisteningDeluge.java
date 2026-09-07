package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "107")
public class GlisteningDeluge extends Card {

    public GlisteningDeluge() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE))));
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetExiledCreatureCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "214")
public class MimeoplasmReveredOne extends Card {

    public MimeoplasmReveredOne() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect(3));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new BecomeCopyOfTargetExiledCreatureCardEffect()),
                "Become a copy of target creature card exiled with this creature, except it's 0/0 and has this ability."));
    }
}

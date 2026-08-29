package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "115")
public class SokenzanSpellblade extends Card {

    public SokenzanSpellblade() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostSelfEffect(new CardsInHand(CountScope.CONTROLLER), new Fixed(0))),
                "{1}{R}: This creature gets +X/+0 until end of turn, where X is the number of cards in your hand."
        ));
    }
}

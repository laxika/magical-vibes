package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneCreatureCardPutIntoGraveyardThisWayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BLB", collectorNumber = "34")
public class StarfallInvocation extends Card {

    public StarfallInvocation() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new EachOtherPlayerDrawsCardEffect(1)));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GiftPromised(),
                new ReturnOneCreatureCardPutIntoGraveyardThisWayEffect()));
    }
}

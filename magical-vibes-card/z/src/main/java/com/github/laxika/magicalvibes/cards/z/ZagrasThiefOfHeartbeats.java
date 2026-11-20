package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "241")
public class ZagrasThiefOfHeartbeats extends Card {

    public ZagrasThiefOfHeartbeats() {
        addStaticEffect(new ReduceOwnCastCostEffect(new PartySize()));
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.otherCreaturesYouControl()));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlaneswalker(), new DestroyTargetPermanentEffect());
    }
}

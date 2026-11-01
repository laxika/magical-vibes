package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesPermanentToExileUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "56")
public class WormfangCrab extends Card {

    public WormfangCrab() {
        addStaticEffect(new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new OpponentChoosesPermanentToExileUntilSourceLeavesEffect(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceEffect());
    }
}

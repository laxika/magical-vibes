package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "98")
public class LichsMastery extends Card {

    public LichsMastery() {
        // "Hexproof"
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));

        // "You can't lose the game."
        addStaticEffect(new CantLoseGameEffect(false));

        // "Whenever you gain life, draw that many cards."
        addTrigger(Triggers.youGainLife(), new DrawCardEffect(new EventValue()));

        // "Whenever you lose life, for each 1 life you lost, exile a permanent you control
        //  or a card from your hand or graveyard."
        addTrigger(Triggers.youLoseLife(), new ExileForEachLifeLostEffect());

        // "When Lich's Mastery leaves the battlefield, you lose the game."
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ControllerLosesGameEffect());
    }
}

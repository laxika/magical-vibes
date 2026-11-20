package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "55")
public class WormfangBehemoth extends Card {

    public WormfangBehemoth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ExileTargetPlayerHandEffect.handsOfWithSource(Subjects.you()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}

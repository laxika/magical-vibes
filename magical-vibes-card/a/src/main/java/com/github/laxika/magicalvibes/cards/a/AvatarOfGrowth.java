package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GNT", collectorNumber = "5")
public class AvatarOfGrowth extends Card {

    public AvatarOfGrowth() {
        addStaticEffect(new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect.mandatory(2));
    }
}

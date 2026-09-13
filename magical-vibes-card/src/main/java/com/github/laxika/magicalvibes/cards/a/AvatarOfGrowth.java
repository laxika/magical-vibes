package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "GNT", collectorNumber = "5")
public class AvatarOfGrowth extends Card {

    public AvatarOfGrowth() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(2));
    }
}

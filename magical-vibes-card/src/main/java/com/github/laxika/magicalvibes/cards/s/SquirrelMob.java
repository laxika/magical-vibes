package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ODY", collectorNumber = "273")
public class SquirrelMob extends Card {

    public SquirrelMob() {
        // This creature gets +1/+1 for each other Squirrel on the battlefield.
        PermanentCount otherSquirrels = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL), CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(otherSquirrels, otherSquirrels));
    }
}

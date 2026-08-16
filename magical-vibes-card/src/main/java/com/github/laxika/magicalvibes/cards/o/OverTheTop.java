package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BRO", collectorNumber = "146")
public class OverTheTop extends Card {

    public OverTheTop() {
        addEffect(EffectSlot.SPELL, new EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect(
                new PermanentCount(new PermanentNotPredicate(new PermanentIsLandPredicate()), CountScope.CONTROLLER)));
    }
}

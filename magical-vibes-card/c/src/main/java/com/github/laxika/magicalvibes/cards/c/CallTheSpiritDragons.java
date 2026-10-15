package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDragonOfEachColorEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "174")
public class CallTheSpiritDragons extends Card {

    public CallTheSpiritDragons() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                0,
                0,
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DRAGON))));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnDragonOfEachColorEffect());
    }
}

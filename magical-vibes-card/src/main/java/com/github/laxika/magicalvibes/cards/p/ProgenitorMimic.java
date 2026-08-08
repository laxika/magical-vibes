package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "92")
public class ProgenitorMimic extends Card {

    public ProgenitorMimic() {
        // You may have this creature enter as a copy of any creature on the battlefield, except it
        // has "At the beginning of your upkeep, if this creature isn't a token, create a token
        // that's a copy of this creature."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature",
                Set.of(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, List.<CardEffect>of(
                        new ConditionalEffect(new NotCondition(new SourceIsToken()),
                                new CreateTokenCopyOfSourceEffect())))
        ));
    }
}

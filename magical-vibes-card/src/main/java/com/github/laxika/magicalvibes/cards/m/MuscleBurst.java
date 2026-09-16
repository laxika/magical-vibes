package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "ODY", collectorNumber = "252")
public class MuscleBurst extends Card {

    public MuscleBurst() {
        var boost = new Sum(
                new Fixed(3),
                new CardsInGraveyard(new CardNamedPredicate("Muscle Burst"), CountScope.ANY_PLAYER));
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(boost, boost));
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "167")
@CardRegistration(set = "DBL", collectorNumber = "434")
public class LambholtRaconteur extends Card {

    public LambholtRaconteur() {
        setBackFaceCard(new LambholtRavager());

        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }

    @Override
    public String getBackFaceClassName() {
        return "LambholtRavager";
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMT", collectorNumber = "144")
@CardRegistration(set = "TMT", collectorNumber = "205")
@CardRegistration(set = "TMT", collectorNumber = "243")
public class DonRaphHardScience extends Card {

    public DonRaphHardScience() {
        addEffect(EffectSlot.ON_ATTACK, new ReduceCastCostForNextMatchingSpellEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
    }
}

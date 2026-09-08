package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "214")
public class ReclusiveTaxidermist extends Card {

    public ReclusiveTaxidermist() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(4, new CardTypePredicate(CardType.CREATURE)),
                new StaticBoostEffect(3, 2, GrantScope.SELF)));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}

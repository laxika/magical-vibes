package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "OPC2", collectorNumber = "38")
public class TrugaJungle extends Card {

    public TrugaJungle() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_LANDS));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new RevealTopCardsMatchingToHandRestToBottomEffect(
                        3, new CardTypePredicate(CardType.LAND)));
    }
}

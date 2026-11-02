package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "38")
@CardRegistration(set = "OPCA", collectorNumber = "81")
@CardRegistration(set = "MOC", collectorNumber = "161")
public class TrugaJungle extends Card {

    public TrugaJungle() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.ALL_LANDS));
        addTrigger(Triggers.chaosEnsues(),
                new RevealTopCardsMatchingToHandRestToBottomEffect(
                        3, new CardTypePredicate(CardType.LAND)));
    }
}

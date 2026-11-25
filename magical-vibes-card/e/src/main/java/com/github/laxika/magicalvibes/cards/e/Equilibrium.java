package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "71")
@CardRegistration(set = "EXO", collectorNumber = "32")
@CardRegistration(set = "MB2", collectorNumber = "162")
public class Equilibrium extends Card {

    public Equilibrium() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.CREATURE)),
                UnlessPaysEffect.youMayPay("{1}", ReturnToHandEffect.target()));
    }
}

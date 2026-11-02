package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "208")
public class NajalTheStormRunner extends Card {

    public NajalTheStormRunner() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.attacks(), new MayPayManaEffect(
                "{2}",
                new CopyNextInstantOrSorceryCastThisTurnEffect(),
                "Pay {2} to copy your next instant or sorcery spell this turn?"));
    }
}

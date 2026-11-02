package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "7")
@CardRegistration(set = "FIN", collectorNumber = "425")
public class AshePrincessOfDalmasca extends Card {

    public AshePrincessOfDalmasca() {
        addTrigger(Triggers.attacks(),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5,
                        new CardTypePredicate(CardType.ARTIFACT)));
    }
}

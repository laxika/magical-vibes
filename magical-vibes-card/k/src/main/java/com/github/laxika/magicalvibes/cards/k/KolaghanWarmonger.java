package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "17")
@CardRegistration(set = "MAT", collectorNumber = "67")
@CardRegistration(set = "MAT", collectorNumber = "117")
@CardRegistration(set = "MAT", collectorNumber = "201")
public class KolaghanWarmonger extends Card {

    public KolaghanWarmonger() {
        addTrigger(Triggers.attacks(), LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardSubtypePredicate(CardSubtype.DRAGON)));
    }
}

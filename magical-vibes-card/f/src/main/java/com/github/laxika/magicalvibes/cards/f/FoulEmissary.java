package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "158")
public class FoulEmissary extends Card {

    public FoulEmissary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                        4, new CardTypePredicate(CardType.CREATURE)));
        addTrigger(Triggers.sacrificed(ObjectRelation.THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE)),
                new CreateTokenEffect(1, "Eldrazi Horror", 3, 2, null,
                        List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false));
    }
}

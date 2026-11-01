package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "72")
@CardRegistration(set = "KLR", collectorNumber = "106")
public class SlyRequisitioner extends Card {

    public SlyRequisitioner() {
        addTrigger(Triggers.dies(null, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardNotPredicate(new CardIsTokenPredicate())
                        )),
                        new TriggeringPermanentControllerConditionalEffect(
                                new CreateTokenEffect(1, "Servo", 1, 1, null,
                                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT)))));
    }
}

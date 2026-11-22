package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "3")
public class SkylineSavior extends Card {

    public SkylineSavior() {
        CardSubject upgraded = Subjects.cardsThisWay(StepKind.RETURN, new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ANGEL)))));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ReturnPermanentControlledByPlayerToHandEffect.controller(new PermanentTruePredicate(), "permanent"),
                        PerpetuallyBoostCardEffect.toCards(upgraded, 1, 1),
                        PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(upgraded, Keyword.FLYING),
                        // Last, as it makes the card an Angel, which the others' "non-Angel" would then not name.
                        new PerpetuallyGrantCardCharacteristicsEffect(Set.of(), Set.of(CardSubtype.ANGEL), List.of())
                                .to(upgraded)));
    }
}

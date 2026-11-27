package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "35")
public class AuthorOfShadows extends Card {

    public AuthorOfShadows() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.allOpponentsWithSource(),
                ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.EXILE,
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))), "a nonland card exiled this way"),
                AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.CHOOSE)).withManaAsThoughAnyColor());
    }
}

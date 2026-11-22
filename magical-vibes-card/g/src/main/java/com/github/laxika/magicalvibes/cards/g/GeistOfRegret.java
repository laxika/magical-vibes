package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "16")
public class GeistOfRegret extends Card {

    public GeistOfRegret() {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        CardTypePredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(instant, sorcery));

        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ChooseCardEffect.atRandom(Subjects.cardsInYourLibrary(instant), "an instant card"),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                ChooseCardEffect.atRandom(Subjects.cardsInYourLibrary(sorcery), "a sorcery card"),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery).castFrom(Zone.GRAVEYARD),
                new CopyTriggeringSpellEffect());
    }
}

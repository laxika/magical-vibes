package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "85")
public class DeliverUntoEvil extends Card {

    public DeliverUntoEvil() {
        CardSubject targets = Subjects.targetGraveyardCards(null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, 4);
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 4);
        addSpellEffect(new ConditionalEffect(new NotCondition(new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsPlaneswalkerPredicate(), new PermanentHasSubtypePredicate(CardSubtype.BOLAS))))),
                ChoosePlayerEffect.opponent()));
        addSpellEffect(new ChooseCardEffect(targets, "two of them to leave in the graveyard", true, new Fixed(2),
                false, List.of()).by(Subjects.playersThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.except(targets, Subjects.cardsThisWay(StepKind.CHOOSE))));
        addSpellEffect(new ExileSpellEffect());
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "98")
@CardRegistration(set = "MSC", collectorNumber = "419")
public class TChakaVenerableKing extends Card {

    public TChakaVenerableKing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.ARTIFACT),
                                        new CardTypePredicate(CardType.LAND)))), 1,
                                "an artifact or land card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileSelfFromGraveyardCost(), new BecomeMonarchEffect()),
                "{3}, Exile this card from your graveyard: You become the monarch. Activate only if you control your commander."
        ).withActivationCondition(
                new ControlsPermanent(new PermanentIsCommanderPredicate(true)),
                "Activate only if you control your commander."
        ));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "184")
public class StubbornBurrowfiend extends Card {

    public StubbornBurrowfiend() {
        CardsInGraveyard creatureCardsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addTrigger(Triggers.becomesSaddled(), new OncePerTurnTriggerEffect(
                SequenceEffect.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new BoostSelfEffect(creatureCardsInGraveyard, creatureCardsInGraveyard))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(2), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 2",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}

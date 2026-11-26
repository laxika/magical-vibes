package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YMID", collectorNumber = "54")
public class RavenousPursuit extends Card {

    public RavenousPursuit() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(DealDamageEffect.bite())
                .addSpellEffect(ChooseCardEffect.oneOf(
                        Subjects.cardsInYourHand(new CardTypePredicate(CardType.CREATURE)),
                        "a creature card in your hand"))
                .addSpellEffect(new PerpetuallyBoostCardEffect(null, new EventValue(), new EventValue(),
                        Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}

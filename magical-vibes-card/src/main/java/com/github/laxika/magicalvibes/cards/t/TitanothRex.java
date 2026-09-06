package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "174")
public class TitanothRex extends Card {

    public TitanothRex() {
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.TRAMPLE,
                                1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate()))),
                        new DrawCardEffect(1)),
                "Cycling {1}{G} ({1}{G}, Discard this card: Draw a card.)",
                TargetFilters.creatureYouControl()));
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "138")
public class NissaVoiceOfZendikar extends Card {

    public NissaVoiceOfZendikar() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        "Plant", 0, 1, CardColor.GREEN, List.of(CardSubtype.PLANT), Set.of(), Set.of())),
                "+1: Create a 0/1 green Plant creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())),
                "−2: Put a +1/+1 counter on each creature you control."
        ));

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new GainLifeEffect(landsYouControl), new DrawCardEffect(landsYouControl)),
                "−7: You gain X life and draw X cards, where X is the number of lands you control."
        ));
    }
}

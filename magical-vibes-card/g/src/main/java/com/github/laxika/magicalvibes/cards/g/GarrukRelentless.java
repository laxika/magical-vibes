package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDealDamageAndReceivePowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "181")
@CardRegistration(set = "INR", collectorNumber = "197")
@CardRegistration(set = "SLD", collectorNumber = "2169")
@CardRegistration(set = "V17", collectorNumber = "9")
@CardRegistration(set = "SIS", collectorNumber = "49")
public class GarrukRelentless extends Card {

    public GarrukRelentless() {
        setBackFaceCard(new GarrukTheVeilCursed());

        // When Garruk Relentless has two or fewer loyalty counters on him, transform him.
        // This is a state-triggered ability (MTG rule 603.8).
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getCounterCount(CounterType.LOYALTY) <= 2 && !sourcePermanent.isTransformed()),
                new TransformSelfEffect());

        // 0: Garruk Relentless deals 3 damage to target creature.
        // That creature deals damage equal to its power to him.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new PlaneswalkerDealDamageAndReceivePowerDamageEffect(3)),
                "0: Garruk Relentless deals 3 damage to target creature. That creature deals damage equal to its power to him.",
                TargetFilters.creature()
        ));

        // 0: Create a 2/2 green Wolf creature token.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of())),
                "0: Create a 2/2 green Wolf creature token."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GarrukTheVeilCursed";
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "ACR", collectorNumber = "8")
@CardRegistration(set = "ACR", collectorNumber = "128")
public class SenuKeenEyedProtector extends Card {

    public SenuKeenEyedProtector() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(), new GainLifeEffect(2), new ScryEffect(2)),
                "{T}, Exile Senu: You gain 2 life and scry 2."
        ));

        addTrigger(new TriggerCondition(Triggers.creatureYouControlAttacksUnblocked()
                        .withFilter(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)), AbilityZones.EXILE),
                TriggerOptions.DEFAULT,
                new ReturnSourceCardFromExileToBattlefieldEffect(false, true));
    }
}
